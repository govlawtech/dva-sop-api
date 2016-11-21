package au.gov.dva.sopref.data;

import au.gov.dva.sopref.interfaces.RegisterClient;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class FederalRegisterOfLegislation implements RegisterClient {

    // Warlike service: https://www.legislation.gov.au/Latest/F2016L00994
    // NonWarlike service: https://www.legislation.gov.au/Details/F2016L00995

    @Override
    public CompletableFuture<byte[]> getAuthorisedInstrumentPdf(String registerId) {
        return null;
    }

    public static URL getAuthorisedDocumentLinkFromHtml(String html, String registerID) throws MalformedURLException {
        Document htmlDocument =  Jsoup.parse(html);
        // Note that currently at legislation.gov.au there is an additional space in the title for this element - probably an error.
        // There is additional selector with one space so this will still work if the devs fix that error.
        String cssSelector = String.format("a[title*=\"%s  authorised version\"], [a[title*=\"%s authorised version\"]", registerID, registerID);
        Elements elements = htmlDocument.select(cssSelector);
        assert !elements.isEmpty();
        String linkUrl =  elements.attr("href");
        assert !linkUrl.isEmpty();
        return URI.create(linkUrl).toURL();
    }
}
