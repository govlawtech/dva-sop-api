package au.gov.dva.sopapi.interfaces;

import java.util.concurrent.CompletableFuture;

public interface ActDeterminationServiceClient {
    CompletableFuture<Boolean> IsOperational(String operationName);
}
