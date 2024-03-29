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
import au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance.AcceptedSequalaeResponse;
import au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance.SequelaeDiagramRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance.SequelaeRequestDto;
import au.gov.dva.sopapi.exceptions.ProcessingRuleRuntimeException;
import au.gov.dva.sopapi.exceptions.ServiceHistoryCorruptException;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.SoPs;
import au.gov.dva.sopapi.sopref.data.PostProcessingFunctions;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopref.data.sops.BasicICDCode;
import au.gov.dva.sopapi.sopref.dependencies.Dependencies;
import au.gov.dva.sopapi.sopref.dependencies.DotToImage;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import au.gov.dva.sopapi.sopsupport.processingrules.IsOperationalPredicateFactory;
import au.gov.dva.sopapi.sopsupport.processingrules.RulesResult;
import au.gov.dva.sopapi.sopsupport.processingrules.PredicateFactory;
import au.gov.dva.sopapi.veaops.interfaces.VeaOperationalServiceRepository;
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

import java.io.ByteArrayOutputStream;
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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static spark.Spark.get;
import static spark.Spark.post;

public class Routes  {

    private final static String MIME_JSON = "application/json";
    private final static String MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final static String MIME_PDF = "application/pdf";
    private final static String MIME_TEXT = "text/plain";
    private final static String MIME_HTML = "text/html";
    private final static String MIME_CSV = "text/csv";
    private final static String MIME_SVG = "image/svg+xml";


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

    }


    public static void init(CacheSingleton cache) {
        Routes.cache = cache;

        get(SharedConstants.Routes.GET_CONDITIONS, (req, res) -> {
            if (validateHeaders() && !responseTypeAcceptable(req, MIME_JSON)) {
                setResponseHeaders(res, 406, MIME_JSON);
                return buildAcceptableContentTypesError(MIME_JSON);
            }

            ConditionsList response = new ConditionsList(cache.get_conditionsList());
            setResponseHeaders(res, 200, MIME_JSON);
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
                setResponseHeaders(res, 406, MIME_JSON);
                return buildAcceptableContentTypesError(MIME_JSON);
            }

            QueryParamsMap queryParamsMap = req.queryMap();
            String startDateString = queryParamsMap.get("startDate").value();
            if (startDateString == null) {
                setResponseHeaders(res, 406, MIME_TEXT);
                return "Need 'startDate' query parameter in ISO local date format.  Eg: 2000-01-01";
            }
            LocalDate startDate;
            try {
                startDate = LocalDate.parse(startDateString, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                setResponseHeaders(res, 406, MIME_TEXT);
                return "Need 'startDate' query parameter in ISO local date format.  Eg: 2000-01-01";
            }

            String endDateString = queryParamsMap.get("endDate").value();
            LocalDate endDate = null;
            if (endDateString == null) {
                endDate = LocalDate.now(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE));
            } else {
                try {
                    endDate = LocalDate.parse(endDateString, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException e) {
                    setResponseHeaders(res, 406, MIME_TEXT);
                    return "Need 'endDate' query parameter in ISO local date format.  Eg: 2000-01-01";
                }
            }

            JsonNode jsonResponse = au.gov.dva.sopapi.veaops.Facade.getResponseRangeQuery(startDate, Optional.of(endDate), cache.get_veaOperationalServiceRepository());
            ObjectMapper om = new ObjectMapper();
            String jsonString = om.writerWithDefaultPrettyPrinter().writeValueAsString(jsonResponse);
            setResponseHeaders(res, 200, MIME_JSON);
            return jsonString;

        });


        get(SharedConstants.Routes.GET_SOPFACTORS, (req, res) -> {

            if (validateHeaders() && !responseTypeAcceptable(req, MIME_JSON)) {
                setResponseHeaders(res, 406, MIME_JSON);
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
                setResponseHeaders(res, 400, MIME_JSON);
                String msg =  "Your request is malformed: \r\n\r\n" + String.join("\r\n", errors);
                ErrorResponseBody errorResponseBody = new ErrorResponseBody(null,"SOP-API-BAD-REQUEST",msg,null);
                return errorResponseBody.toJson();
            }

            ImmutableSet<SoP> matchingSops;
            if (conditionName != null || !conditionName.isEmpty()) {
                matchingSops = SoPs.getMatchingSopsByConditionName(conditionName, cache.get_allSops(), OffsetDateTime.now());
            } else {
                matchingSops = SoPs.getMatchingSopsByIcdCode(new BasicICDCode(icdCodeVersion, icdCodeValue), cache.get_allSops(), OffsetDateTime.now());
            }

            if (matchingSops.isEmpty()) {
                setResponseHeaders(res, 200, MIME_JSON);
                // return empty array  - MyService devs especially wanted this
                return "{\"applicableFactors\": []}";
            } else {

                setResponseHeaders(res, 200, MIME_JSON);

                IncidentType it = IncidentType.fromString(incidentType);
                StandardOfProof sp = StandardOfProof.fromAbbreviation(standardOfProof);

                List<Function<SoPReferenceResponse, SoPReferenceResponse>> postProcessors = new ArrayList<>();
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
                setResponseHeaders(res, 400, MIME_JSON);
                ErrorResponseBody errorResponseBody = new ErrorResponseBody(null, "SOP-API-BAD-REQUEST", e.getMessage(),null);

                return errorResponseBody.toJson();
            }

            ImmutableList<String> semanticErrors = SemanticRequestValidation.getSemanticErrors(sopSupportRequestDto);
            if (!semanticErrors.isEmpty()) {
                setResponseHeaders(res, 400, MIME_JSON);


                String detailMessage = String.format("Request body invalid: %n%s", String.join(scala.util.Properties.lineSeparator(), semanticErrors));
                ErrorResponseBody errorResponseBody = new ErrorResponseBody(null, "SOP-API-BAD-REQUEST", detailMessage,null);

                return errorResponseBody.toJson();
            }

            if (sopSupportRequestDto.get_conditionDto().get_conditionName() == null) {
                Optional<String> conditionNameFromICDCode = getConditionNameForICDCode(sopSupportRequestDto.get_conditionDto().get_icdCodeVersion(), sopSupportRequestDto.get_conditionDto().get_icdCodeValue(), cache.get_allSopPairs());
                if (!conditionNameFromICDCode.isPresent()) {
                    setResponseHeaders(res, 400, MIME_JSON);
                    String detailMessage = String.format("The given ICD code and version does not map to a single SoP.");
                    ErrorResponseBody errorResponseBody = new ErrorResponseBody(null, "SOP-API-BAD-REQUEST", detailMessage,null);
                    return errorResponseBody.toJson();
                } else {
                    sopSupportRequestDto.get_conditionDto().set_conditionName(conditionNameFromICDCode.get());
                }
            }

            ServiceDeterminationPair serviceDeterminationPair = Operations.getLatestDeterminationPair(cache.get_allMrcaServiceDeterminations());

            IsOperationalPredicateFactory isOperationalPredicateFactory = new PredicateFactory(serviceDeterminationPair, cache.get_veaOperationalServiceRepository());
            // todo: new up conditionFactory here

            RulesResult rulesResult = runRules(cache.get_ruleConfigurationRepository(), sopSupportRequestDto, isOperationalPredicateFactory, cache.get_veaOperationalServiceRepository(), cache.get_allMrcaServiceDeterminations());
            SopSupportResponseDto sopSupportResponseDto = rulesResult.buildSopSupportResponseDto();
            setResponseHeaders(res, 200, MIME_JSON);
            return SopSupportResponseDto.toJsonString(sopSupportResponseDto);
        }));

        get(SharedConstants.Routes.SEQUELAE_DIAGRAM + "/:conditionName", (req, res) -> {
            QueryParamsMap queryParamsMap = req.queryMap();
            String conditionName = req.params(":conditionName");
            String steps = queryParamsMap.get("steps").value();
            List<String> conditionNames = cache.get_conditionsList().stream().map(c -> c.get_conditionName()).sorted().collect(Collectors.toList());
            String conditionNamesStringList = String.join(scala.util.Properties.lineSeparator(), conditionNames);
            if (conditionName == null) {
                setResponseHeaders(res, 400, MIME_JSON);
                StringBuilder sb = new StringBuilder();
                sb.append("Missing: query parameter 'conditionName' with one of the following values:");
                sb.append(scala.util.Properties.lineSeparator());
                sb.append(conditionNamesStringList);
                return sb.toString();
            }
            if (!conditionNames.contains(conditionName)) {
                setResponseHeaders(res, 400, MIME_TEXT);
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("Condition name '%s' not recognised.", conditionName));
                sb.append(scala.util.Properties.lineSeparator());
                sb.append("Recognised names are:");
                sb.append(scala.util.Properties.lineSeparator());
                sb.append(conditionNamesStringList);
                return sb.toString();
            }
            int stepsInt;
            if (steps == null) {
                stepsInt = 1;
            } else {
                try {
                    stepsInt = Integer.parseInt(steps);
                    if (stepsInt < 1 || stepsInt > 3) {
                        setResponseHeaders(res, 400, MIME_TEXT);
                        return "Query parameter 'steps' must be between 0 and 3 inclusive.";
                    }
                } catch (NumberFormatException e) {
                    setResponseHeaders(res, 400, MIME_TEXT);
                    return "Query parameter 'steps' must be an integer.";
                }
            }
            String dotString = cache.get_dependencies().getDotSubgraph(conditionName, stepsInt);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DotToImage.render(dotString, bos);
            byte[] svg = bos.toByteArray();
            bos.close();
            setResponseHeaders(res, 200, MIME_SVG);
            res.header("Content-Disposition", String.format("attachment; filename=\"%s\"",conditionName + ".svg"));
            return svg;

        });

        sopPost(SharedConstants.Routes.SEQUELAE_RECOMMENDATIONS, MIME_JSON, ((req, res) -> {
            SequelaeRequestDto sequleeRequestDto;
            try {

                sequleeRequestDto = SequelaeRequestDto.fromJsonString(clenseJson(req.body()));
                // validate conditions exist
                ImmutableSet<String> allValidConditionNames = cache.get_allSopPairs().stream().map(c -> c.getConditionName()).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));

                List<String> acceptedConditionNames = sequleeRequestDto.get_acceptedConditions().stream().map(c -> c.get_name()).collect(Collectors.toList());
                List<String> diagnosedConditionNames = sequleeRequestDto.get_diagnosedConditions().stream().map(c -> c.get_name()).collect(Collectors.toList());
                ImmutableSet<String> allConditionNames = ImmutableSet.<String>builder().addAll(acceptedConditionNames).addAll(diagnosedConditionNames).build();

                ImmutableList<String> validationErrors = sequleeRequestDto.getValidationErrors(allValidConditionNames, allConditionNames);
                if (!validationErrors.isEmpty()) {
                    setResponseHeaders(res, 400, MIME_TEXT);
                    return String.join(scala.util.Properties.lineSeparator(), validationErrors);
                }

                AcceptedSequalaeResponse response = Dependencies.getInferredSequelae(sequleeRequestDto, cache.get_allSopPairs());
                setResponseHeaders(res, 200, MIME_JSON);
                return response.toJsonString();


            } catch (DvaSopApiDtoRuntimeException e) {
                setResponseHeaders(res, 400, MIME_TEXT);
                return String.format("Request body invalid: %s", e.getMessage());
            }

        }));

        sopPost(SharedConstants.Routes.SEQUELAE_DIAGRAM, MIME_SVG, ((req, res) -> {
            SequelaeDiagramRequestDto sequelaeDiagramRequestDto;
            try {

                sequelaeDiagramRequestDto = SequelaeDiagramRequestDto.fromJsonString(clenseJson(req.body()));
                // validate conditions exist
                ImmutableSet<String> allValidConditionNames = cache.get_allSopPairs().stream().map(c -> c.getConditionName()).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));

                List<String> acceptedConditionNames = sequelaeDiagramRequestDto.get_acceptedConditions().stream().collect(Collectors.toList());
                List<String> diagnosedConditionNames = sequelaeDiagramRequestDto.get_diagnosedConditions().stream().collect(Collectors.toList());
                ImmutableSet<String> allConditionNames = ImmutableSet.<String>builder().addAll(acceptedConditionNames).addAll(diagnosedConditionNames).build();


                ImmutableList<String> validationErrors = SequelaeRequestDto.getValidationErrors(allValidConditionNames, allConditionNames);
                if (!validationErrors.isEmpty()) {
                    setResponseHeaders(res, 400, MIME_TEXT);
                    return String.join(scala.util.Properties.lineSeparator(), validationErrors);
                }

                byte[] svg = Dependencies.getSvg(sequelaeDiagramRequestDto, cache.get_allSopPairs());
                setResponseHeaders(res, 200, MIME_SVG);
                res.header("Content-Disposition", "attachment; filename=\"sequelae.svg\"");
                return svg;


            } catch (DvaSopApiDtoRuntimeException e) {
                setResponseHeaders(res, 400, MIME_TEXT);
                return String.format("Request body invalid: %s", e.getMessage());
            }

        }));


    }


    // Set up a post handler with response MIME type handling and exception handling.
    private static void sopPost(String path, String responseMimeType, Route handler) {
        post(path, ((req, res) ->
        {
            if (validateHeaders() && !responseTypeAcceptable(req, responseMimeType)) {
                setResponseHeaders(res, 406, MIME_JSON);
                return buildAcceptableContentTypesError(responseMimeType);
            }

            try {
                return handler.handle(req, res);

            } catch (ServiceHistoryCorruptException e) {
                logger.error("Service history corrupt.", e);
                setResponseHeaders(res, 400, MIME_JSON);
                ErrorResponseBody errorResponseBody = new ErrorResponseBody(null, "SOP-API-BAD-REQUEST","Service history corrupt: " + e.getMessage(), null);
                return errorResponseBody.toJson();
            } catch (ProcessingRuleRuntimeException e) {
                logger.error("Error applying rule.", e);
                setResponseHeaders(res, 500, MIME_JSON);
                ErrorResponseBody errorResponseBody = new ErrorResponseBody(null, "SOP-API-INTERNAL-RULE-LOGIC-ERROR",null,null);
                return errorResponseBody.toJson();
            } catch (Exception e) {
                ErrorResponseBody errorResponseBody = new ErrorResponseBody(null, "SOP-API-INTERNAL-ERROR",null,null);
                logger.error("Unknown exception", e);
                setResponseHeaders(res, 500, MIME_JSON);
                return errorResponseBody.toJson();
            } catch (Error e) {
                logger.error("Unknown error", e);
                ErrorResponseBody errorResponseBody = new ErrorResponseBody(null, "SOP-API-INTERNAL-ERROR",null,null);
                setResponseHeaders(res, 500, MIME_JSON);
                return errorResponseBody.toJson();
            }
        }));
    }


    public static RulesResult runRules(RuleConfigurationRepository repository, SopSupportRequestDto sopSupportRequestDto, IsOperationalPredicateFactory isOperationalPredicateFactory, VeaOperationalServiceRepository veaOperationalServiceRepository, ImmutableSet<ServiceDetermination> serviceDeterminations) {
        CaseTrace caseTrace = new SopSupportCaseTrace();
        caseTrace.setConditionName(sopSupportRequestDto.get_conditionDto().get_conditionName());

        RulesResult rulesResult = RulesResult.applyRules(repository, sopSupportRequestDto, cache.get_allSopPairs(),
                isOperationalPredicateFactory, veaOperationalServiceRepository, serviceDeterminations, caseTrace);

        assert caseTrace.isComplete() : "Case trace not complete";
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
        if (responseType.equals(MIME_JSON) || responseType.equals(MIME_JSON)) {
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
        String msg =  "Accept header in request must include '" + mimeType + "'.";
        ErrorResponseBody errorResponseBody = new ErrorResponseBody(null,"SOP-API-BAD-REQUEST", msg,null);
        return errorResponseBody.toJson();
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
