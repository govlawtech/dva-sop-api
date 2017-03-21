package au.gov.dva.sopapi;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoError;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.QueryParamLabels;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponse;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import au.gov.dva.sopapi.exceptions.ProcessingRuleError;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.interfaces.model.casesummary.CaseSummaryModel;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.SoPs;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopref.data.sops.BasicICDCode;
import au.gov.dva.sopapi.sopsupport.ConditionFactory;
import au.gov.dva.sopapi.sopsupport.SopSupport;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import au.gov.dva.sopapi.sopsupport.casesummary.CaseSummary;
import au.gov.dva.sopapi.sopsupport.casesummary.CaseSummaryModelImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import au.gov.dva.sopapi.sopsupport.processingrules.RulesResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static spark.Spark.get;
import static spark.Spark.post;

class Routes {

    private static Cache cache;
    static Logger logger = LoggerFactory.getLogger(Routes.class);

    public static void initStatus(Repository repository, Cache cache) {

        get("/status", (req, res) -> {
            StringBuilder sb = new StringBuilder();

            ImmutableSet<SoPPair> soPPairs = SoPs.groupSopsToPairs(cache.get_allSops());

            Optional<OffsetDateTime> lastUpdated = repository.getLastUpdated();

            String lastUpdateTime = lastUpdated.isPresent() ? lastUpdated.get().toString() : "Unknown";

            setResponseHeaders(res, false, 200);

            List<String> conditionList = soPPairs.stream().map(sp -> "* " + sp.getConditionName()).sorted().collect(toList());
            String conditionsListString = String.join("\r\n", conditionList);
            sb.append(String.format("Number of conditions available: %d%n", conditionList.size()));
            sb.append(String.format("Last checked for updated SoPs and Service Determinations: %s%n", lastUpdateTime));
            sb.append(String.format("Condition available:\r\n"));
            sb.append(conditionsListString);


            return sb.toString();

        });
    }


    public static void init(Cache cache) {
        Routes.cache = cache;


        get(SharedConstants.Routes.GET_OPERATIONS, (req, res) -> {

            if (validateHeaders() && !responseTypeAcceptable(req)) {
                setResponseHeaders(res, false, 406);
                return buildAcceptableContentTypesError();
            }

            ServiceDeterminationPair latestServiceDeterminationPair = Operations.getLatestDeterminationPair(cache.get_allServiceDeterminations());

            OperationsResponse operationsResponse = DtoTransformations.buildOperationsResponseDto(latestServiceDeterminationPair);

            setResponseHeaders(res, true, 200);
            String json = OperationsResponse.toJsonString(operationsResponse);
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


        post(SharedConstants.Routes.GET_SERVICE_CONNECTION, ((req, res) -> {
            if (validateHeaders() && !responseTypeAcceptable(req)) {
                setResponseHeaders(res, false, 406);
                return buildAcceptableContentTypesError();
            }
            SopSupportRequestDto sopSupportRequestDto;
            try {
                sopSupportRequestDto = SopSupportRequestDto.fromJsonString(cleanseJson(req.body()));
            } catch (DvaSopApiDtoError e) {
                setResponseHeaders(res, false, 400);
                return buildIncorrectRequestFromatError();
            }


            RulesResult rulesResult;

            try {
                rulesResult = runRules(sopSupportRequestDto);
            } catch (ProcessingRuleError e) {
                logger.error("Error applying rule.", e);
                setResponseHeaders(res, false, 500);
                return e.getMessage();
            }

            SopSupportResponseDto sopSupportResponseDto = SopSupport.buildSopSupportResponseDtoFromRulesResult(rulesResult);

            setResponseHeaders(res, true, 200);
            return SopSupportResponseDto.toJsonString(sopSupportResponseDto);


        }));

        post(SharedConstants.Routes.GET_CASESUMMARY, ((req, res) ->

        {
            if (validateHeaders() && !responseTypeAcceptableDocx(req)) {
                setResponseHeaders(res, false, 406);
                return buildAcceptableContentTypesDocxError();
            }

            try {
                SopSupportRequestDto sopSupportRequestDto = SopSupportRequestDto.fromJsonString(cleanseJson(req.body()));

                RulesResult rulesResult;
                try {
                    rulesResult = runRules(sopSupportRequestDto);
                } catch (ProcessingRuleError e) {
                    logger.error("Error applying rule.", e);
                    setResponseHeaders(res, false, 500);
                    return e.getMessage();
                }

                if (rulesResult.isEmpty()) {
                    setResponseHeaders(res, false, 204);
                    return "No applicable rules.";
                }
                ServiceHistory serviceHistory = DtoTransformations.serviceHistoryFromDto(sopSupportRequestDto.get_serviceHistoryDto());
                Condition condition = rulesResult.getCondition().get();

                List<Factor> factorsConnectedToService = rulesResult.getFactorWithSatisfactions().stream()
                        .filter(f -> f.isSatisfied())
                        .map(f -> f.getFactor())
                        .collect(toList());

                CaseSummaryModel model = new CaseSummaryModelImpl(condition, serviceHistory, rulesResult.getApplicableSop().get(), ImmutableSet.copyOf(factorsConnectedToService) );
                byte[] result = CaseSummary.createCaseSummary(model, buildIsOperationalPredicate()).get();

                setResponseHeadersDocXResponse(res);
                return result;
            } catch (DvaSopApiDtoError e) {
                setResponseHeaders(res, false, 400);
                return buildIncorrectRequestFromatError();
            } catch (ProcessingRuleError e) {
                logger.error("Error applying rule.", e);
                setResponseHeaders(res, false, 500);
                return e.getMessage();
            }
        }));

    }

    private static String buildIncorrectRequestFromatError() {
        Optional<String> schema = generateSchemaForSopSupportRequestDto();
        StringBuilder sb = new StringBuilder();
        if (schema.isPresent()) {
            sb.append("Request body does not conform to expected schema:\n");
            sb.append(schema);
        }
        return sb.toString();
    }

    private static RulesResult runRules(SopSupportRequestDto sopSupportRequestDto) {
        ImmutableSet<SoPPair> soPPairs = SoPs.groupSopsToPairs(cache.get_allSops());

        Optional<Condition> condition = ConditionFactory.create(soPPairs, sopSupportRequestDto.get_conditionDto(), cache.get_ruleConfigurationRepository());
        if (!condition.isPresent()) {
            return RulesResult.createEmpty();
        }
        else {

            CaseTrace caseTrace = new SopSupportCaseTrace(UUID.randomUUID().toString());

            RulesResult rulesResult = SopSupport.applyRules(cache.get_ruleConfigurationRepository(), sopSupportRequestDto, soPPairs, buildIsOperationalPredicate(), caseTrace);

            if (AppSettings.getEnvironment().isDev())
                logger.trace(caseTrace.toString());

            return rulesResult;
        }
    }

    private static Predicate<Deployment> buildIsOperationalPredicate() {
        ServiceDeterminationPair serviceDeterminationPair = Operations.getLatestDeterminationPair(cache.get_allServiceDeterminations());
        Predicate<Deployment> isOperational = ProcessingRuleFunctions.getIsOperationalPredicate(serviceDeterminationPair);
        return isOperational;
    }

    private static List<String> getSopParamsValidationErrors(String icdCodeValue, String icdCodeVersion, String
            standardOfProof, String conditionname, String incidentType) {
        List<String> errors = new ArrayList<>();

        if (conditionname == null) {
            String missingICDCodeError = "Need ICD code (query parameter '" + QueryParamLabels.ICD_CODE_VALUE + "') and ICD code version (query parameter '" + QueryParamLabels.ICD_CODE_VERSION + "') if condition name (query parameter '" + QueryParamLabels.CONDITION_NAME + "') is not provided.";
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

    private static void setResponseHeadersDocXResponse(Response response) {
        response.status(200);
        response.type("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

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

    private static boolean responseTypeAcceptableDocx(Request request) {
        String contentTypeHeader = request.headers("Accept");
        if (contentTypeHeader == null)
            return false;
        if (contentTypeHeader.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
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

    private static String buildAcceptableContentTypesDocxError() {
        return "Accept header in request must include 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'.";
    }

    private static Boolean validateHeaders() {
        return AppSettings.getEnvironment() == AppSettings.Environment.prod;
    }

    private static String cleanseJson(String incomingJson) {
        return incomingJson.replace("\uFEFF", "");
    }
}
