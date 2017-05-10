package au.gov.dva.sopapi.interfaces;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RegisterClient {
    CompletableFuture<byte[]> getLatestDocxInstrument(String registerId);

    CompletableFuture<Optional<String>> getRepealingRegisterId(String repealedRegisterId);
    CompletableFuture<byte[]> getLatestAuthorisedInstrumentPdf(String registerId);
    CompletableFuture<String> getRedirectTargetRegisterId(String sourceRegisterId);
}
