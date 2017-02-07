package au.gov.dva.sopapi;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoError;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.QueryParamLabels;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponseDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import au.gov.dva.sopapi.exceptions.ProcessingRuleError;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.SoPs;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopref.data.sops.BasicICDCode;
import au.gov.dva.sopapi.sopsupport.SopSupport;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static spark.Spark.get;

class Routes {

    private static Cache cache;
    static Logger logger = LoggerFactory.getLogger(Routes.class);

    public static void init(Cache cache)
    {
        Routes.cache = cache;

        get("/hello", (req, res) -> {
            return "Hello";
        });

        get(SharedConstants.Routes.GET_OPERATIONS, (req, res) -> {

            if (validateHeaders() && !responseTypeAcceptable(req)) {
                setResponseHeaders(res, false, 406);
                return buildAcceptableContentTypesError();
            }

            ServiceDeterminationPair latestServiceDeterminationPair = Operations.getLatestDeterminationPair(cache.get_allServiceDeterminations());

            OperationsResponseDto operationsResponseDto = DtoTransformations.buildOperationsResponseDto(latestServiceDeterminationPair);

            setResponseHeaders(res, true, 200);
            String json = OperationsResponseDto.toJsonString(operationsResponseDto);
            return json;
        });

        get(SharedConstants.Routes.GET_SOPFACTORS, (req, res) -> {

            if (validateHeaders() && !responseTypeAcceptable(req)) {
                setResponseHeaders(res, false, 406);
                return buildAcceptableContentTypesError();
            }

            QueryParamsMap queryParamsMap = req.queryMap();
            String icdCodeValue = queryParamsMap.get("icdCodeValue").value();
            String icdCodeVersion = queryParamsMap.get("icdCodeVersion").value();
            String standardOfProof = queryParamsMap.get("standardOfProof").value(); // todo: make optional
            String conditionName = queryParamsMap.get("conditionName").value();
            String incidentType = queryParamsMap.get("incidentType").value();

            List<String> errors = getSopParamsValidationErrors(icdCodeValue, icdCodeVersion, standardOfProof, conditionName, incidentType);

            if (errors.size() > 0) {
                setResponseHeaders(res, false, 400);
                return "Your request is malformed: \r\n\r\n" + String.join("\r\n", errors);
            }

            ImmutableSet<SoP> matchingSops = SoPs.getMatchingSops(conditionName, new BasicICDCode(icdCodeVersion, icdCodeValue), cache.get_allSops());

            if (matchingSops.isEmpty()) {
                setResponseHeaders(res, false, 404);
                return buildErrorMessageShowingRecognisedIcdCodesAndConditionNames(cache.get_allSops());
            } else {

                setResponseHeaders(res, true, 200);

                IncidentType it = IncidentType.fromString(incidentType);
                StandardOfProof sp = StandardOfProof.fromAbbreviation(standardOfProof);

                String response = SoPs.buildSopRefJsonResponse(matchingSops, it, sp);
                return response;
            }
        });

        get(SharedConstants.Routes.GET_SERVICE_CONNECTION, ((req, res) -> {
            if (validateHeaders() && !responseTypeAcceptable(req)) {
                setResponseHeaders(res, false, 406);
                return buildAcceptableContentTypesError();
            }

            try {
                SopSupportRequestDto sopSupportRequestDto = SopSupportRequestDto.fromJsonString(cleanseJson(req.body()));

                ImmutableSet<SoPPair> soPPairs = SoPs.groupSopsToPairs(cache.get_allSops());
                ImmutableSet<ServiceDetermination> serviceDeterminations = cache.get_allServiceDeterminations();
                ServiceDeterminationPair serviceDeterminationPair = Operations.getLatestDeterminationPair(serviceDeterminations);
                Predicate<Deployment> isOperational = ProcessingRuleFunctions.getIsOperationalPredicate(serviceDeterminationPair);
                CaseTrace caseTrace = new SopSupportCaseTrace(UUID.randomUUID().toString());
                caseTrace.addTrace(req.body());
                SopSupportResponseDto sopSupportResponseDto = SopSupport.applyRules(sopSupportRequestDto, soPPairs, isOperational, caseTrace);
                if (AppSettings.getEnvironment().isDev())
                    logger.trace(caseTrace.toString());
                setResponseHeaders(res, true, 200);
                return SopSupportResponseDto.toJsonString(sopSupportResponseDto);
            } catch (DvaSopApiDtoError e) {
                setResponseHeaders(res, false, 400);
                Optional<String> schema = generateSchemaForSopSupportRequestDto();
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%s\n", e.getMessage()));
                if (schema.isPresent()) {
                    sb.append("Request body does not conform to expected schema:\n");
                    sb.append(schema);
                }
                return sb.toString();
            }
            catch (ProcessingRuleError e)
            {
                logger.error("Error applying rule.", e);
                setResponseHeaders(res,false,400);
                return e.getMessage();
            }
        }));
    }

    private static List<String> getSopParamsValidationErrors(String icdCodeValue, String icdCodeVersion, String standardOfProof, String conditionname, String incidentType) {
        List<String> errors = new ArrayList<>();

        if (conditionname == null) {
            String missingICDCodeError = "Need ICD code (query parameter '" + QueryParamLabels.ICD_CODE_VALUE + "') and ICD code version (query paramater '" + QueryParamLabels.ICD_CODE_VERSION + "') if condition name (query parameter '" + QueryParamLabels.CONDITION_NAME + "') is not provided.";
            if (icdCodeValue == null)
                errors.add(buildQueryParamErrorMessage(QueryParamLabels.ICD_CODE_VALUE, missingICDCodeError));

            if (icdCodeVersion == null) {
                errors.add(buildQueryParamErrorMessage(QueryParamLabels.ICD_CODE_VERSION, missingICDCodeError));
            }
        }

        if (standardOfProof == null)
            errors.add(buildQueryParamErrorMessage(QueryParamLabels.STANDARD_OF_PROOF, "required, missing."));

        else {
            if (!standardOfProof.contentEquals("RH") && !standardOfProof.contentEquals("BoP"))
                errors.add(buildQueryParamErrorMessage(QueryParamLabels.STANDARD_OF_PROOF, "acceptable values are 'RH' (for Reasonable Hypothesis) and 'BoP' (for Balance of Probabilities)."));
        }

        if (incidentType == null)
            errors.add(buildQueryParamErrorMessage(QueryParamLabels.INCIDENT_TYPE, "required, missing."));
        else {
            if (!incidentType.contentEquals("aggravation") && !incidentType.contentEquals("onset"))
                errors.add(buildQueryParamErrorMessage(QueryParamLabels.INCIDENT_TYPE, "acceptable values are 'aggravation' and 'onset'."));
        }

        return errors;
    }


    private static String buildQueryParamErrorMessage(String queryParamName, String msg) {
        return String.format("* Query paramater '%s': %s", queryParamName, msg);
    }

    private static String buildErrorMessageShowingRecognisedIcdCodesAndConditionNames(ImmutableSet<SoP> sops) {
        String recognisedConditionNames = String.join("\r\n", sops.stream().map(soP -> "* " + soP.getConditionName()).sorted().distinct().collect(toList()));

        String recognisedICDCodes = String.join("\r\n", sops.stream().flatMap(soP -> soP.getICDCodes().stream())
                .map(code -> String.format("* %s %s", code.getVersion(), code.getCode()))
                .distinct()
                .collect(toList()));

        StringBuilder sb = new StringBuilder();
        sb.append("The condition name and ICD code (if any) you provided did not match any in the database.\r\n\r\n");
        sb.append("Known condition names:\r\n");
        sb.append("======================\r\n");
        sb.append(recognisedConditionNames);
        sb.append("\r\n\r\n");
        sb.append("Known ICD codes:\r\n");
        sb.append("================\r\n");
        sb.append(recognisedICDCodes);

        return sb.toString();

    }

    private static void setResponseHeaders(Response response, Boolean isJson, Integer statusCode) {
        response.status(statusCode);
        if (isJson) {
            response.type("application/json; charset=utf-8");
        } else {
            response.type("text/plain; charset=utf-8");
        }

        response.header("X-Content-Type-Options", "nosniff");

    }

    private static boolean responseTypeAcceptable(Request request) {
        String contentTypeHeader = request.headers("Accept");
        if (contentTypeHeader == null)
            return false;
        if (contentTypeHeader.contains("application/json"))
            return true;
        else return false;
    }

    private static Optional<String> generateSchemaForSopSupportRequestDto() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        try {
            JsonSchema schema = schemaGen.generateSchema(SopSupportRequestDto.class);
            String schemaString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
            return Optional.of(schemaString);
        } catch (JsonMappingException e) {
            logger.error("Failed to generate schema for request DTO for SoP Support Service.");
            return Optional.empty();
        } catch (JsonProcessingException e) {
            logger.error("Failed to generate schema for request DTO for SoP Support Service.");
            return Optional.empty();
        }
    }

    private static String buildAcceptableContentTypesError() {
        return "Accept header in request must include 'application/json'.";
    }

    private static Boolean validateHeaders() {
        return AppSettings.getEnvironment() == AppSettings.Environment.prod;
    }

    private static String cleanseJson(String incomingJson)
    {
        return incomingJson.replace("\uFEFF","");
    }
}
