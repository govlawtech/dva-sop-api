package au.gov.dva.sopapi.sopref.data;

import au.gov.dva.sopapi.exceptions.LegislationRegisterError;
import au.gov.dva.sopapi.interfaces.RegisterClient;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static org.asynchttpclient.Dsl.*;

import org.asynchttpclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class FederalRegisterOfLegislation implements RegisterClient {

    // Warlike service: https://www.legislation.gov.au/Latest/F2016L00994
    // NonWarlike service: https://www.legislation.gov.au/Details/F2016L00995

    final static Logger logger = LoggerFactory.getLogger(FederalRegisterOfLegislation.class);

    @Override
    public CompletableFuture<byte[]> getAuthorisedInstrumentPdf(String registerId) throws ExecutionException, InterruptedException {
        URL latestDownloadPageUrl;
        try {
            latestDownloadPageUrl = new URL(buildUrlForLatestDownloadPage(registerId));
        }
        catch (MalformedURLException e) {
            throw new LegislationRegisterError(e);
        }

        CompletableFuture<byte[]> promise = getRedirectTarget(latestDownloadPageUrl)
                .thenCompose(url -> downloadHtml(url) )
                .thenApply(htmlString -> {
                    try {
                        return getAuthorisedDocumentLinkFromHtml(htmlString, registerId);
                    } catch (MalformedURLException e) {
                        throw new LegislationRegisterError(e);
                    }
                })
                .thenCompose(url -> downloadFile(url));

        return promise;
    }

    public static CompletableFuture<byte[]> downloadFile(URL url) {
        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        CompletableFuture<byte[]> promise = asyncHttpClient
                .prepareGet(url.toString())
                .execute()
                .toCompletableFuture()
                .thenApply(response -> response.getResponseBodyAsBytes());
        return promise;
    }

    public static CompletableFuture<String> downloadHtml(URL url) {
        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        CompletableFuture<String> promise = asyncHttpClient
                .prepareGet(url.toString())
                .execute()
                .toCompletableFuture()
                .thenApply(response -> response.getResponseBody());
        return promise;
    }

    public static CompletableFuture<URL> getRedirectTarget(URL originalUrl) {
        assert (originalUrl.getHost().startsWith("www"));
        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        CompletableFuture<URL> promise = asyncHttpClient
                .prepareGet(originalUrl.toString())
                .execute()
                .toCompletableFuture()
                .thenApply(response -> {
                    assert (response.getStatusCode() == 302);
                    String redirectValue = response.getHeader("Location");
                if (redirectValue.isEmpty())
                        throw new LegislationRegisterError(String.format("Could not get redirect to Details page from URL: %s\n%s", originalUrl.toString(), response.toString()));
                    try {
                        assert (!URI.create(redirectValue).isAbsolute() && redirectValue.startsWith("/"));
                        URL   redirectTarget =  URI.create(String.format("%s://%s%s", originalUrl.getProtocol(), originalUrl.getHost(), redirectValue)).toURL();
                        return redirectTarget;
                    } catch (MalformedURLException e) {
                        throw new LegislationRegisterError(e);
                    }
                });

        return promise;
    }

    public static URL getAuthorisedDocumentLinkFromHtml(String html, String registerID) throws MalformedURLException {
        Document htmlDocument = Jsoup.parse(html);
        // Note that currently at legislation.gov.au there is an additional space in the title for this element - probably an error.
        // There is additional selector with one space so this will still work if the devs fix that error.
        String cssSelector = String.format("a[title*=\"%s  authorised version\"], [a[title*=\"%s authorised version\"]", registerID, registerID);
        Elements elements = htmlDocument.select(cssSelector);
        assert !elements.isEmpty();
        String linkUrl = elements.attr("href");
        assert !linkUrl.isEmpty();
        return URI.create(linkUrl).toURL();
    }

    private static String buildUrlForLatestDownloadPage(String registerId) {
        return String.format("https://www.legislation.gov.au/Latest/%s/Download", registerId);
    }

}
