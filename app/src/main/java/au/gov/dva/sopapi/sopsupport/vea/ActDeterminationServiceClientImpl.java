package au.gov.dva.sopapi.sopsupport.vea;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import au.gov.dva.sopapi.exceptions.ActDeterminationServiceException;
import au.gov.dva.sopapi.interfaces.ActDeterminationServiceClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;




public class ActDeterminationServiceClientImpl implements ActDeterminationServiceClient {

    private static final AsyncHttpClient asyncHttpClient =  new DefaultAsyncHttpClient();

    private final String baseUrl;

    public ActDeterminationServiceClientImpl(String baseUrl) {
        this.baseUrl = baseUrl;
    }


    @Override
    public CompletableFuture<Boolean> matchesWhiteFilter(String operationName, Predicate<List<ServiceRegion>> whiteFilter) {
        CompletableFuture<Boolean> promise =
                asyncHttpClient.preparePost(baseUrl + "/operation")
                        .setBody(String.format("{\"operationName\":\"%s\"}",operationName))
                        .setHeader("content-type","application/json;charset=UTF-8")
                        .setHeader("content-length","350")
                        .execute()
                        .toCompletableFuture()
                        .thenApply(response -> {
                                    if (response.getStatusCode() == 200) {
                                        return deserialiseResponse(response.getResponseBody());
                                    } else {
                                        throw new ActDeterminationServiceException("Status code received from Acts Determination Service: " + response.getStatusCode());
                                    }
                                }
                        )
                        .thenApply(whiteFilter::test);

        return promise;
    }

    @Override
    public CompletableFuture<Boolean> isOperational(String operationName) {

        return matchesWhiteFilter(operationName, this::inferWhetherOperational);

    }




    boolean inferWhetherOperational(List<ServiceRegion> actDeterminationServiceResponse) {
        return actDeterminationServiceResponse.stream()
                .anyMatch(operationJsonResponse ->
                        (operationJsonResponse.isOperational() ||
                         operationJsonResponse.isWarlike() ||
                         operationJsonResponse.isHazardous() ||
                         operationJsonResponse.isPeacekeeping()) &&
                         !operationJsonResponse.isMrcaNonWarlike() &&
                         !operationJsonResponse.isMrcaWarlike());
    }

    private List<ServiceRegion> deserialiseResponse(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        try {
            List<ServiceRegion> actDeterminationServiceResponse = objectMapper.readValue(json, new TypeReference<List<ServiceRegion>>() {});
            return actDeterminationServiceResponse;
        } catch (IOException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }

    }

}
