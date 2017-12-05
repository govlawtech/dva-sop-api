package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.sopsupport.vea.OperationJsonResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface ActDeterminationServiceClient {

    CompletableFuture<Boolean> matchesWhiteFilter(String operationName, Predicate<List<OperationJsonResponse>> whiteFilter);
    CompletableFuture<Boolean> isOperational(String operationName);
}
