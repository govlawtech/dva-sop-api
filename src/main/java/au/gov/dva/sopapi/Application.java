package au.gov.dva.sopapi;

import au.gov.dva.sopapi.SharedConstants.Routes;
import au.gov.dva.sopapi.dtos.DvaSopApiDtoError;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.QueryParamLabels;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponseDto;
import au.gov.dva.sopapi.dtos.sopsupport.RequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.ResponseDto;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.SoPs;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.sops.BasicICDCode;
import au.gov.dva.sopapi.sopsupport.SopSupport;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static spark.Spark.get;

public class Application implements spark.servlet.SparkApplication {

    private ImmutableSet<SoP> _allSops;
    private ImmutableSet<ServiceDetermination> _allServiceDeterminations;

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public Application() {
        Repository repository = new AzureStorageRepository(au.gov.dva.sopapi.AppSettings.AzureStorage.getConnectionString());
        _allSops = repository.getAllSops();
        _allServiceDeterminations = repository.getServiceDeterminations();
    }

    @Override
    public void init() {
        get("/hello", (req, res) -> {
            if (!responseTypeAcceptable(req)) {
                setResponseHeaders(res, false, 406);
                return buildAcceptableContentTypesError();
            }
            return "Hello";
        })
        ;

        get(Routes.GET_OPERATIONS, (req, res) -> {

            if (!responseTypeAcceptable(req)) {
                setResponseHeaders(res, false, 406);
                return buildAcceptableContentTypesError();
            }

            QueryParamsMap queryParamMap = req.queryMap();
            String queryDate = queryParamMap.get(QueryParamLabels.QUERY_DATE).value();
            if (queryDate == null) {
                setResponseHeaders(res, false, 400);
                return buildQueryParamErrorMessage(QueryParamLabels.QUERY_DATE, "required, missing");
            }
            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(queryDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                setResponseHeaders(res, false, 400);
                return buildQueryParamErrorMessage(QueryParamLabels.QUERY_DATE, "Date must be in ISO local date format: yyyy-mm-dd. For example, 2017-01-01.");
            }

            ImmutableSet<ServiceDetermination> latestServiceDeterminationPair = Operations.getLatestDeterminationPair(_allServiceDeterminations, parsedDate);

            OperationsResponseDto operationsResponseDto = DtoTransformations.buildOperationsResponseDto(latestServiceDeterminationPair);

            setResponseHeaders(res, true, 200);
            String json = OperationsResponseDto.toJsonString(operationsResponseDto);
            return json;

        });

        get(Routes.GET_SOPFACTORS, (req, res) -> {

            if (!responseTypeAcceptable(req)) {
                setResponseHeaders(res, false, 406);
                return buildAcceptableContentTypesError();
            }

            QueryParamsMap queryParamsMap = req.queryMap();
            String icdCodeValue = queryParamsMap.get("icdCodeValue").value();
            String icdCodeVersion = queryParamsMap.get("icdCodeVersion").value();
            String standardOfProof = queryParamsMap.get("standardOfProof").value();
            String conditionName = queryParamsMap.get("conditionName").value();
            String incidentType = queryParamsMap.get("incidentType").value();

            List<String> errors = getSopParamsValidationErrors(icdCodeValue, icdCodeVersion, standardOfProof, conditionName, incidentType);

            if (errors.size() > 0) {
                setResponseHeaders(res, false, 400);
                return "Your request is malformed: \r\n\r\n" + String.join("\r\n", errors);
            }

            ImmutableSet<SoP> matchingSops = SoPs.getMatchingSops(conditionName, new BasicICDCode(icdCodeVersion, icdCodeValue), _allSops);

            if (matchingSops.isEmpty()) {
                setResponseHeaders(res, false, 404);
                return buildErrorMessageShowingRecognisedIcdCodesAndConditionNames(_allSops);
            } else {

                setResponseHeaders(res, true, 200);

                IncidentType it = IncidentType.fromString(incidentType);
                StandardOfProof sp = StandardOfProof.fromAbbreviation(standardOfProof);

                String response = SoPs.buildSopRefJsonResponse(matchingSops, it, sp);
                return response;
            }
        });

        get(Routes.GET_SERVICE_CONNECTION, ((req, res) -> {
            if (!responseTypeAcceptable(req)) {
                setResponseHeaders(res, false, 406);
                return buildAcceptableContentTypesError();
            }

            try {
                RequestDto requestDto = RequestDto.fromJsonString(req.body());
                ResponseDto responseDto = SopSupport.applyRules(requestDto,_allSops,_allServiceDeterminations);
                setResponseHeaders(res,true,200);
                return responseDto;
            }
            catch (DvaSopApiDtoError e) {
                setResponseHeaders(res,false,400);
                Optional<String> schema = generateSchemaForSopSupportRequestDto();
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%s\n", e.getMessage()));
                if (schema.isPresent())
                {
                    sb.append("Request body does not conform to expected schema:\n");
                    sb.append(schema);
                }
                return sb.toString();

            }

        }));

    }



    private static List<String> getSopParamsValidationErrors(String icdCodeValue, String icdCodeVersion, String standardOfProof, String conditionname, String incidentType) {
        List<String> errors = new ArrayList<>();

        if (conditionname == null) {
            String missingICDCodeError = "Need ICD code (query parameter '" + QueryParamLabels.ICD_CODE_Value + "') and ICD code version (query paramater '" + QueryParamLabels.ICD_CODE_VERSION + "') if condition name (query parameter '" + QueryParamLabels.CONDITION_NAME + "') is not provided.";
            if (icdCodeValue == null)
                errors.add(buildQueryParamErrorMessage(QueryParamLabels.ICD_CODE_Value, missingICDCodeError));

            if (icdCodeVersion == null) {
                errors.add(buildQueryParamErrorMessage(QueryParamLabels.ICD_CODE_VERSION, missingICDCodeError));
            }
        }

        if (standardOfProof == null)
            errors.add(buildQueryParamErrorMessage(QueryParamLabels.STANDARD_OF_PROOF, "required, missing."));

        else {
            if (!standardOfProof.contentEquals("RH") && !standardOfProof.contentEquals("BoP"))
                errors.add(buildQueryParamErrorMessage(QueryParamLabels.STANDARD_OF_PROOF, "acceptable values are 'RH' (for Reasonable Hypthesis) and 'BoP' (for Balance of Probabilities)."));
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
        String recognisedConditionNames = String.join("\r\n", sops.stream().map(soP -> "* " + soP.getConditionName()).sorted().collect(toList()));

        String recognisedICDCodes = String.join("\r\n", sops.stream().flatMap(soP -> soP.getICDCodes().stream())
                .map(code -> String.format("* %s %s", code.getVersion(), code.getCode()))
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

    //todo: scheduled task to refresh cache of SoPs from Repository
    // todo: scheduled task to update Repository from Legislation Register


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
            mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING,true);
            JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
            try {
                JsonSchema schema = schemaGen.generateSchema(RequestDto.class);
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
}
