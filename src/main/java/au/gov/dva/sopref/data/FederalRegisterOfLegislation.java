package au.gov.dva.sopref.data;

import au.gov.dva.sopref.interfaces.RegisterClient;

import java.util.concurrent.CompletableFuture;

public class FederalRegisterOfLegislation implements RegisterClient {

    @Override
    public CompletableFuture<byte[]> getAuthorisedInstrumentPdf(String registerId) {
        return null;
    }
}
