package au.gov.dva.sopapi;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.QueryParamLabels;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.ConditionsList;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponse;
import au.gov.dva.sopapi.dtos.sopref.SoPReferenceResponse;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import au.gov.dva.sopapi.exceptions.ProcessingRuleRuntimeException;
import au.gov.dva.sopapi.exceptions.ServiceHistoryCorruptException;
import au.gov.dva.sopapi.interfaces.ActDeterminationServiceClient;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.SoPs;
import au.gov.dva.sopapi.sopref.data.PostProcessingFunctions;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopref.data.sops.BasicICDCode;
import au.gov.dva.sopapi.sopref.dependencies.Dependencies;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import au.gov.dva.sopapi.sopsupport.processingrules.IRhPredicateFactory;
import au.gov.dva.sopapi.sopsupport.processingrules.RhPredicateFactory;
import au.gov.dva.sopapi.sopsupport.processingrules.RulesResult;
import au.gov.dva.sopapi.sopsupport.processingrules.SuperiorRhPredicateFactory;
import au.gov.dva.sopapi.sopsupport.vea.ActDeterminationServiceClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static spark.Spark.get;
import static spark.Spark.post;

public class Routes {

    private final static String MIME_JSON = "application/json";
    private final static String MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final static String MIME_PDF = "application/pdf";
    private final static String MIME_TEXT = "text/plain";
    private final static String MIME_HTML = "text/html";
    private final static String MIME_CSV = "text/csv";

    private static CacheSingleton cache;
    static Logger logger = LoggerFactory.getLogger("dvasopapi.webapi");


    public static void initStatus(Repository repository, CacheSingleton cache) {

        get("/refreshCache", (req, res) -> {

            QueryParamsMap queryParamsMap = req.queryMap();
            String expectedKey = AppSettings.getCacheRefreshKey();
            if (expectedKey == null) {
                setResponseHeaders(res, 404, MIME_TEXT);
                return "Set 'CACHE_REFRESH_KEY' environment variable on server.";
            }

            String receivedKey = queryParamsMap.get("key").value();
            if (receivedKey == null) {

                setResponseHeaders(res, 400, MIME_TEXT);
                return "Missing required query parameter: 'key'.";
            }

            if (receivedKey.contentEquals(expectedKey)) {
                cache.refresh(repository);
                setResponseHeaders(res, 200, MIME_TEXT);
                return "Cache refreshed.";
            } else {
                setResponseHeaders(res, 403, MIME_TEXT);
                return "Key does not match";
            }

        });

        get("/status", (req, res) -> {

            Optional<URI> blobStorageUri = getBaseUrlForBlobStorage();
            if (!blobStorageUri.isPresent()) {
                logger.error("Need blob storage URI for status page.");
                res.status(500);
            }

            // Read version
            Properties p = new Properties();
            p.load(req.raw().getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
            String version = p.getProperty("Implementation-Version");

            // todo: mustache template
            String statusPage = Status.createStatusHtml(cache, repository, blobStorageUri.get().toURL(), version);
            setResponseHeaders(res, 200, MIME_HTML);
            return statusPage;
        });


        get("/status/csv", (req, res) -> {

            Optional<URI> blobStorageUri = getBaseUrlForBlobStorage();
            if (!blobStorageUri.isPresent()) {
                logger.error("Need blob storage URI for status page.");
                res.status(500);
            }

            String csvFileName = String.format("SoP API Coverage Report Generated UTC %s.csv", OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-dd-M-HH-mm")));
            byte[] csvBytes = Status.createStatusCsv(cache, blobStorageUri.get().toURL());
            res.header("Content-disposition", String.format("attachment;filename=%s", csvFileName));
            setResponseHeaders(res, 200, MIME_CSV);
            return csvBytes;
        });

        post("/status/dependencies", (req,res) -> {

           ImmutableSet<String> conditions = ImmutableSet.copyOf(req.body().split(scala.util.Properties.lineSeparator()));
           String dotString = Dependencies.buildDotString(cache.get_allSopPairs(),conditions);
           //setResponseHeaders(res, 200, MIME_TEXT);
           //return dotString;

            return "SANITY!";
        });
    }


    public static void init(CacheSingleton cache) {
        Routes.cache = cache;

        get(SharedConstants.Routes.GET_CONDITIONS, (req,res) -> {
            if (validateHeaders() && !responseTypeAcceptable(req, MIME_JSON)) {
                setResponseHeaders(res, 406, MIME_TEXT);
                return buildAcceptableContentTypesError(MIME_JSON);
            }

            ConditionsList response = new ConditionsList(cache.get_conditionsList());
            setResponseHeaders(res,200,MIME_JSON);
            String json = ConditionsList.toJsonString(response);
            return json;
        });

        get(SharedConstants.Routes.GET_OPERATIONS, (req, res) -> {

            if (validateHeaders() && !responseTypeAcceptable(req, MIME_JSON)) {
                setResponseHeaders(res, 406, MIME_TEXT);
                return buildAcceptableContentTypesError(MIME_JSON);
            }

            ServiceDeterminationPair latestServiceDeterminationPair = Operations.getLatestDeterminationPair(cache.get_allMrcaServiceDeterminations());

            OperationsResponse operationsResponse = DtoTransformations.buildOperationsResponseDto(latestServiceDeterminationPair);

            setResponseHeaders(res, 200, MIME_JSON);
            String json = OperationsResponse.toJsonString(operationsResponse);
            return json;
        });

        get(SharedConstants.Routes.GET_VEA_ACTIVITIES, (req, res) -> {
            if (validateHeaders() && !responseTypeAcceptable(req, MIME_JSON)) {
                setResponseHeaders(res, 406, MIME_TEXT);
                return buildAcceptableContentTypesError(MIME_JSON);
            }

            QueryParamsMap queryParamsMap = req.queryMap();
            String startDateString = queryParamsMap.get("startDate").value();
            if (startDateString == null)
            {
                setResponseHeaders(res,406,MIME_TEXT);
                return "Need 'startDate' query parameter in ISO local date format.  Eg: 2000-01-01";
            }
            LocalDate startDate;
            try {
               startDate = LocalDate.parse(startDateString, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            catch (DateTimeParseException e)
            {
                setResponseHeaders(res,406,MIME_TEXT);
                return "Need 'startDate' query parameter in ISO local date format.  Eg: 2000-01-01";
            }

            String endDateString = queryParamsMap.get("endDate").value();
            LocalDate endDate = null;
            if (endDateString == null)
            {
                endDate = LocalDate.now(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE));
            }
            else {
                try {
                    endDate = LocalDate.parse(endDateString, DateTimeFormatter.ISO_LOCAL_DATE);
                }
                catch (DateTimeParseException e)
                {
                    setResponseHeaders(res,406,MIME_TEXT);
                    return "Need 'endDate' query parameter in ISO local date format.  Eg: 2000-01-01";
                }
            }

            JsonNode jsonResponse = au.gov.dva.sopapi.veaops.Facade.getResponseRangeQuery(startDate,endDate,cache.get_veaOperationalServiceRepository());
            ObjectMapper om = new ObjectMapper();
            String jsonString = om.writerWithDefaultPrettyPrinter().writeValueAsString(jsonResponse);
            setResponseHeaders(res,200,MIME_JSON);
            return jsonString;

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

                List<Function<SoPReferenceResponse,SoPReferenceResponse>> postProcessors = new ArrayList<>();
                if (cache.get_curatedTextReporitory().isPresent()) {
                    postProcessors.add(PostProcessingFunctions.SubInCuratedFactorText(cache.get_curatedTextReporitory().get()));
                    postProcessors.add(PostProcessingFunctions.SubInCuratedDefinitions(cache.get_curatedTextReporitory().get()));
                }
                String response = SoPs.buildSopRefJsonResponse(matchingSops, it, sp, ImmutableList.copyOf(postProcessors));
                return response;
            }
        });


        sopPost(SharedConstants.Routes.GET_SERVICE_CONNECTION, MIME_JSON, ((req, res) -> {
            SopSupportRequestDto sopSupportRequestDto;
            try {
                sopSupportRequestDto = SopSupportRequestDto.fromJsonString(clenseJson(req.body()));
            } catch (DvaSopApiDtoRuntimeException e) {
                setResponseHeaders(res, 400, MIME_TEXT);
                return String.format("Request body invalid: %s", e.getMessage());
            }

            ImmutableList<String> semanticErrors = SemanticRequestValidation.getSemanticErrors(sopSupportRequestDto);
            if (!semanticErrors.isEmpty()) {
                setResponseHeaders(res, 400, MIME_TEXT);
                return String.format("Request body invalid: %n%s", String.join(scala.util.Properties.lineSeparator(), semanticErrors));
            }

            if (sopSupportRequestDto.get_conditionDto().get_conditionName() == null) {
                Optional<String> conditionNameFromICDCode = getConditionNameForICDCode(sopSupportRequestDto.get_conditionDto().get_icdCodeVersion(), sopSupportRequestDto.get_conditionDto().get_icdCodeValue(), cache.get_allSopPairs());
                if (!conditionNameFromICDCode.isPresent()) {
                    setResponseHeaders(res, 400, MIME_TEXT);
                    return String.format("The given ICD code and version does not map to a single SoP.");
                } else {
                    sopSupportRequestDto.get_conditionDto().set_conditionName(conditionNameFromICDCode.get());
                }
            }

            ServiceDeterminationPair serviceDeterminationPair = Operations.getLatestDeterminationPair(cache.get_allMrcaServiceDeterminations());
            IRhPredicateFactory rhPredicateFactory = new SuperiorRhPredicateFactory(serviceDeterminationPair,cache.get_veaOperationalServiceRepository());
            // todo: new up conditionFactory here

            RulesResult rulesResult = runRules(sopSupportRequestDto, rhPredicateFactory);
            SopSupportResponseDto sopSupportResponseDto = rulesResult.buildSopSupportResponseDto();
            setResponseHeaders(res, 200, MIME_JSON);
            return SopSupportResponseDto.toJsonString(sopSupportResponseDto);
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
            } catch (DvaSopApiDtoRuntimeException e) {
                setResponseHeaders(res, 400, MIME_TEXT);
                return buildIncorrectRequestFormatError(e.getMessage());
            }
            catch (ServiceHistoryCorruptException e)
            {
                logger.error("Service history corrupt.",e);
                setResponseHeaders(res,400,MIME_TEXT);
                return e.getMessage();
            }
            catch (ProcessingRuleRuntimeException e) {
                logger.error("Error applying rule.", e);
                setResponseHeaders(res, 500, MIME_TEXT);
                return e.getMessage();
            } catch (Exception e) {
                logger.error("Unknown exception", e);
                setResponseHeaders(res, 500, MIME_TEXT);
                return e.getMessage();
            } catch (Error e) {
                logger.error("Unknown error", e);
                setResponseHeaders(res, 500, MIME_TEXT);
                return e.getMessage();
            }
        }));
    }

    private static String buildIncorrectRequestFormatError(String detailedErrorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(detailedErrorMessage);
        Optional<String> schema = generateSchemaForSopSupportRequestDto();
        if (schema.isPresent()) {
            sb.append("Request must conform to schema:\n");
            sb.append(schema);
        }
        return sb.toString();
    }

    private static RulesResult runRules(SopSupportRequestDto sopSupportRequestDto, IRhPredicateFactory rhPredicateFactory) {
        CaseTrace caseTrace = new SopSupportCaseTrace(UUID.randomUUID().toString());
        caseTrace.setConditionName(sopSupportRequestDto.get_conditionDto().get_conditionName());

        // todo: inject condition factory
        RulesResult rulesResult = RulesResult.applyRules(cache.get_ruleConfigurationRepository(), sopSupportRequestDto, cache.get_allSopPairs(),
                rhPredicateFactory.createMrcaOrVeaPredicate(sopSupportRequestDto.get_conditionDto()), caseTrace);
        return rulesResult;
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
        return "Accept header in request must include '" + mimeType + "'.";
    }

    private static Boolean validateHeaders() {
        return AppSettings.getEnvironment() == Environment.prod;
    }

    private static String clenseJson(String incomingJson) {
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

    private static Optional<String> getConditionNameForICDCode(String icdCodeVersion, String icdCode, ImmutableSet<SoPPair> sopPairs) {
        List<SoPPair> sopPairsMatchingIcdCode = sopPairs.stream()
                .filter(soPPair -> soPPair.getICDCodes().stream()
                        .anyMatch(icdCode1 -> icdCode1.getCode().contentEquals(icdCode) && icdCode1.getVersion().contentEquals(icdCodeVersion)))
                .collect(toList());

        if (sopPairsMatchingIcdCode.size() == 1) {
            return Optional.of(sopPairsMatchingIcdCode.get(0).getConditionName());
        } else return Optional.empty();
    }
}
