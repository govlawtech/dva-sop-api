package au.gov.dva.sopref.interfaces;

import java.util.concurrent.CompletableFuture;

public interface RegisterClient {
    CompletableFuture<byte[]> getAuthorisedInstrumentPdf(String registerId);
}
