package au.gov.dva.sopref.data;

import au.gov.dva.sopref.interfaces.RegisterClient;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class FederalRegisterOfLegislation implements RegisterClient {

    // Warlike service: https://www.legislation.gov.au/Latest/F2016L00994
    // NonWarlike service: https://www.legislation.gov.au/Details/F2016L00995

    @Override
    public CompletableFuture<byte[]> getAuthorisedInstrumentPdf(String registerId) {
        return null;
    }


    public static URL getAuthorisedDocumentLinkFromHtml(String html)
    {
        return null;
    }
}
