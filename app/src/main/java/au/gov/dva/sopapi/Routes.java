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
import com.google.common.collect.ImmutableSet;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static spark.Spark.get;
import static spark.Spark.post;

class Routes {

    private final static String MIME_JSON = "application/json";
    private final static String MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final static String MIME_PDF = "application/pdf";
    private final static String MIME_TEXT = "text/plain";
    private final static String MIME_HTML = "text/html";
    private final static String MIME_CSV = "text/csv";

    private static Cache cache;
    static Logger logger = LoggerFactory.getLogger("dvasopapi.webapi");

    public static void initStatus(Repository repository, Cache cache) {

        get("/status", (req, res) -> {

            Optional<URI> blobStorageUri = getBaseUrlForBlobStorage();
            if (!blobStorageUri.isPresent()) {
                logger.error("Need blob storage URI for status page.");
                res.status(500);
            }

            // todo: mustache template
            String statusPage = Status.createStatusHtml(cache,repository,blobStorageUri.get().toURL());
            setResponseHeaders(res,200,MIME_HTML);
            return statusPage;
        });


        get("/status/csv",(req,res) -> {

            Optional<URI> blobStorageUri = getBaseUrlForBlobStorage();
            if (!blobStorageUri.isPresent()) {
                logger.error("Need blob storage URI for status page.");
                res.status(500);
            }

            String csvFileName = String.format("SoP API Coverage Report Generated UTC %s.csv", OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-dd-M-HH-mm")));
            byte[] csvBytes = Status.createStatusCsv(cache, blobStorageUri.get().toURL());
            res.header("Content-disposition", String.format("attachment;filename=%s",csvFileName));
            setResponseHeaders(res, 200, MIME_CSV);
            return csvBytes;
        });
    }



    public static void init(Cache cache) {
        Routes.cache = cache;

        get(SharedConstants.Routes.GET_OPERATIONS, (req, res) -> {

            if (validateHeaders() && !responseTypeAcceptable(req, MIME_JSON)) {
                setResponseHeaders(res, 406, MIME_TEXT);
                return buildAcceptableContentTypesError(MIME_JSON);
            }

            ServiceDeterminationPair latestServiceDeterminationPair = Operations.getLatestDeterminationPair(cache.get_allServiceDeterminations());

            OperationsResponse operationsResponse = DtoTransformations.buildOperationsResponseDto(latestServiceDeterminationPair);

            setResponseHeaders(res, 200, MIME_JSON);
            String json = OperationsResponse.toJsonString(operationsResponse);
            return json;
        });

        get(SharedConstants.Routes.GET_SOPFACTORS, (req, res) -> {

            if (validateHeaders() && !responseTypeAcceptable(req, MIME_JSON)) {
                setResponseHeaders(res, 406, MIME_TEXT);
                return buildAcceptableContentTypesError(MIME_JSON);
            }

            QueryParamsMap queryParamsMap = req.queryMap();
            String icdCodeValue = queryParamsMap.get("icdCodeValue").value();
            String icdCodeVersion = queryParamsMap.get("icdCodeVersion").value();
            String standardOfProof = queryParamsMap.get("standardOfProof").value(); // todo: make optional
            String conditionName = queryParamsMap.get("conditionName").value();
            String incidentType = queryParamsMap.get("incidentType").value();

            List<String> errors = getSopParamsValidationErrors(icdCodeValue, icdCodeVersion, standardOfProof, conditionName, incidentType);

            if (errors.size() > 0) {
                setResponseHeaders(res, 400, MIME_TEXT);
                return "Your request is malformed: \r\n\r\n" + String.join("\r\n", errors);
            }

            ImmutableSet<SoP> matchingSops;
            if (conditionName != null || !conditionName.isEmpty()) {
                matchingSops = SoPs.getMatchingSopsByConditionName(conditionName, cache.get_allSops(), OffsetDateTime.now());
            } else {
                matchingSops = SoPs.getMatchingSopsByIcdCode(new BasicICDCode(icdCodeVersion, icdCodeValue), cache.get_allSops(), OffsetDateTime.now());
            }

            if (matchingSops.isEmpty()) {
                setResponseHeaders(res, 404, MIME_TEXT);
                return buildErrorMessageShowingRecognisedIcdCodesAndConditionNames(cache.get_allSops());
            } else {

                setResponseHeaders(res, 200, MIME_JSON);

                IncidentType it = IncidentType.fromString(incidentType);
                StandardOfProof sp = StandardOfProof.fromAbbreviation(standardOfProof);

                String response = SoPs.buildSopRefJsonResponse(matchingSops, it, sp);
                return response;
            }
        });


        sopPost(SharedConstants.Routes.GET_SERVICE_CONNECTION, MIME_JSON, ((req, res) -> {
            SopSupportRequestDto sopSupportRequestDto = SopSupportRequestDto.fromJsonString(cleanseJson(req.body()));
            RulesResult rulesResult = runRules(sopSupportRequestDto);
            SopSupportResponseDto sopSupportResponseDto = rulesResult.buildSopSupportResponseDto();
            setResponseHeaders(res, 200, MIME_JSON);
            return SopSupportResponseDto.toJsonString(sopSupportResponseDto);
        }));

        sopPost(SharedConstants.Routes.GET_CASESUMMARY, MIME_DOCX, ((req, res) ->
        {
            byte[] result;
            SopSupportRequestDto sopSupportRequestDto = SopSupportRequestDto.fromJsonString(cleanseJson(req.body()));
            RulesResult rulesResult = runRules(sopSupportRequestDto);

            if (rulesResult.isEmpty()) {
                result = CaseSummary.createCaseSummary(rulesResult.getCaseTrace(), buildIsOperationalPredicate(), false).get();
            }
            else {
                ServiceHistory serviceHistory = DtoTransformations.serviceHistoryFromDto(sopSupportRequestDto.get_serviceHistoryDto());
                Condition condition = rulesResult.getCondition().get();

                List<Factor> factorsConnectedToService = rulesResult.getSatisfiedFactors();

                CaseSummaryModel model = new CaseSummaryModelImpl(condition, serviceHistory, rulesResult.getApplicableSop().get(), ImmutableSet.copyOf(factorsConnectedToService), rulesResult.getCaseTrace(), rulesResult.getRecommendation() );
                result = CaseSummary.createCaseSummary(model, buildIsOperationalPredicate(), false).get();
            }

            setResponseHeaders(res, 200, MIME_DOCX);
            return result;
        }));

        sopPost(SharedConstants.Routes.GET_CASESUMMARY_AS_PDF, MIME_PDF, ((req, res) ->
        {
            byte[] result;
            SopSupportRequestDto sopSupportRequestDto = SopSupportRequestDto.fromJsonString(cleanseJson(req.body()));
            RulesResult rulesResult = runRules(sopSupportRequestDto);

            if (rulesResult.isEmpty()) {
                result = CaseSummary.createCaseSummary(rulesResult.getCaseTrace(), buildIsOperationalPredicate(), true).get();
            }
            else {
                ServiceHistory serviceHistory = DtoTransformations.serviceHistoryFromDto(sopSupportRequestDto.get_serviceHistoryDto());
                Condition condition = rulesResult.getCondition().get();

                List<Factor> factorsConnectedToService = rulesResult.getSatisfiedFactors();

                CaseSummaryModel model = new CaseSummaryModelImpl(condition, serviceHistory, rulesResult.getApplicableSop().get(), ImmutableSet.copyOf(factorsConnectedToService), rulesResult.getCaseTrace(), rulesResult.getRecommendation() );
                result = CaseSummary.createCaseSummary(model, buildIsOperationalPredicate(), true).get();
            }

            setResponseHeaders(res, 200, MIME_PDF);
            return result;
        }));
    }

    // Set up a post handler with response MIME type handling and exception handling.
    private static void sopPost(String path, String responseMimeType, Route handler) {
        post(path, ((req, res) ->
        {
            if (validateHeaders() && !responseTypeAcceptable(req, responseMimeType)) {
                setResponseHeaders(res, 406, MIME_TEXT);
                return buildAcceptableContentTypesError(responseMimeType);
            }

            try {
                return handler.handle(req, res);
            }
            catch (DvaSopApiDtoError e) {
                setResponseHeaders(res, 400, MIME_TEXT);
                return buildIncorrectRequestFormatError();
            }
            catch (ProcessingRuleError e) {
                logger.error("Error applying rule.", e);
                setResponseHeaders(res, 500, MIME_TEXT);
                return "";
            }
            catch (Exception e) {
                logger.error("Unknown exception", e);
                setResponseHeaders(res, 500, MIME_TEXT);
                return "";
            }
            catch (Error e) {
                logger.error("Unknown error", e);
                setResponseHeaders(res, 500, MIME_TEXT);
                return "";
            }
        }));
    }

    private static String buildIncorrectRequestFormatError() {
        Optional<String> schema = generateSchemaForSopSupportRequestDto();
        StringBuilder sb = new StringBuilder();
        if (schema.isPresent()) {
            sb.append("Request body does not conform to expected schema:\n");
            sb.append(schema);
        }
        return sb.toString();
    }

    private static RulesResult runRules(SopSupportRequestDto sopSupportRequestDto) {
        CaseTrace caseTrace = new SopSupportCaseTrace(UUID.randomUUID().toString());
        RulesResult rulesResult = RulesResult.applyRules(cache.get_ruleConfigurationRepository(), sopSupportRequestDto, cache.get_allSopPairs(), buildIsOperationalPredicate(), caseTrace);
        return rulesResult;
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

    private static void setResponseHeaders(Response response, Integer statusCode, String mimeType) {
        response.status(statusCode);

        String responseType = mimeType;
        if (responseType.equals(MIME_JSON) || responseType.equals(MIME_TEXT)) {
            responseType += "; charset=utf-8";
        }
        response.type(responseType);

        response.header("X-Content-Type-Options", "nosniff");
    }

    private static boolean responseTypeAcceptable(Request request, String mimeType) {
        String contentTypeHeader = request.headers("Accept");
        if (contentTypeHeader == null)
            return false;
        if (contentTypeHeader.contains(mimeType))
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

    private static String buildAcceptableContentTypesError(String mimeType) {
        return "Accept header in request must include '"+mimeType+"'.";
    }

    private static Boolean validateHeaders() {
        return AppSettings.getEnvironment() == Environment.prod;
    }

    private static String cleanseJson(String incomingJson) {
        return incomingJson.replace("\uFEFF", "");
    }

    private static Optional<URI> getBaseUrlForBlobStorage() {
        try {
            String _storageConnectionString = AppSettings.AzureStorage.getConnectionString();
            CloudStorageAccount _cloudStorageAccount = CloudStorageAccount.parse(_storageConnectionString);
            CloudBlobClient _cloudBlobClient = _cloudStorageAccount.createCloudBlobClient();
            return Optional.of(_cloudBlobClient.getEndpoint());

        } catch (InvalidKeyException | URISyntaxException e) {
            logger.error("Cannot get base url for blog storage.", e);
            return Optional.empty();
        }
    }
}
