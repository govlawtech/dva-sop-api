package au.gov.dva.sopref.interfaces;

import java.util.concurrent.CompletableFuture;

public interface FederalRegisterOfLegislation {
    CompletableFuture<byte[]> getAuthorisedInstrument(String registerId);
}
