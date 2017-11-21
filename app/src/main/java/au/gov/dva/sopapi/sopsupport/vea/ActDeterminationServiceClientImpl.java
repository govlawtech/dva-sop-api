package au.gov.dva.sopapi.sopsupport.vea;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.interfaces.ActDeterminationServiceClient;
import au.gov.dva.sopapi.interfaces.model.Operation;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ActDeterminationServiceClientImpl implements ActDeterminationServiceClient {

    private static final AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();

    private final String baseUrl;

    public ActDeterminationServiceClientImpl(String baseUrl) {
        this.baseUrl = baseUrl;
    }


    @Override
    public CompletableFuture<Boolean> IsOperational(String operationName) {
        CompletableFuture<Boolean> promise =
                asyncHttpClient.preparePost(baseUrl + "/operation")
                        .execute()
                        .toCompletableFuture()
                        .thenApply(response -> {
                                    if (response.getStatusCode() == 200) {
                                        return deserialiseResponse(response.getResponseBody());
                                    } else {
                                        throw new DvaSopApiDtoRuntimeException("Status code received from Acts Determination Service: " + response.getStatusCode());
                                    }
                                }
                        )
                        .thenApply(actDeterminationServiceResponse -> inferWhetherOperational(actDeterminationServiceResponse));

        return promise;

    }


    private boolean inferWhetherOperational(ActDeterminationServiceResponse actDeterminationServiceResponse) {
        return actDeterminationServiceResponse.getOperations().stream()
                .anyMatch(operationJsonResponse ->
                        operationJsonResponse.isOperational() ||
                                operationJsonResponse.isWarlike() ||
                                operationJsonResponse.isHazardous());
    }

    private ActDeterminationServiceResponse deserialiseResponse(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        try {
            ActDeterminationServiceResponse actDeterminationServiceResponse = objectMapper.readValue(json, ActDeterminationServiceResponse.class);
            return actDeterminationServiceResponse;
        } catch (IOException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }

    }

}
