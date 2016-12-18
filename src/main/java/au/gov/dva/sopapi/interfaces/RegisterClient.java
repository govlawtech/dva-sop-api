package au.gov.dva.sopapi.interfaces;

import java.net.MalformedURLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface RegisterClient {
    CompletableFuture<byte[]> getAuthorisedInstrumentPdf(String registerId) throws MalformedURLException, ExecutionException, InterruptedException;
}
