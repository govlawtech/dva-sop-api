package au.gov.dva.sopapi.sopref.data;

import au.gov.dva.sopapi.exceptions.LegislationRegisterError;
import au.gov.dva.sopapi.interfaces.RegisterClient;
import org.asynchttpclient.AsyncHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class FederalRegisterOfLegislationClient implements RegisterClient {

    private static final String BASE_URL = "https://www.legislation.gov.au";

    final static Logger logger = LoggerFactory.getLogger(FederalRegisterOfLegislationClient.class);

    private static AsyncHttpClient asyncHttpClient = asyncHttpClient();
    @Override
    public CompletableFuture<String> getRedirectTargetRegisterId(String registerId) {
        URL urlForWhichToGetRedirect = BuildUrl.toGetRedirect(registerId);
        return getRedirectTargetUrl(urlForWhichToGetRedirect)
                .thenApply(url -> extractTargetRegisterIdFromRedirectUrl(url));

    }

    public static String extractTargetRegisterIdFromRedirectUrl(URL redirectTargetUrl) {
        String[] pathParts = redirectTargetUrl.getPath().split("/");
        return pathParts[pathParts.length - 1];
    }

    @Override
    public CompletableFuture<byte[]> getLatestAuthorisedInstrumentPdf(String registerId) {
     // todo: refactor redirects out of this - no longer need them
        URL latestDownloadPageUrl;
        latestDownloadPageUrl = BuildUrl.forLatestDownloadPage(registerId);
        CompletableFuture<byte[]> promise = getRedirectTargetUrl(latestDownloadPageUrl)
                .thenCompose(url -> downloadHtml(url))
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


    @Override
    public CompletableFuture<byte[]> getLatestDocxInstrument(String registerId) {
        URL urlForDownloadPage = BuildUrl.forDownloadPage(registerId);

        CompletableFuture<byte[]> promise =  downloadHtml(urlForDownloadPage)
                .thenApply(htmlString -> {
                    try {
                        return getDocxDocumentLinkFromHtml(htmlString, registerId);
                    } catch (MalformedURLException e) {
                        throw new LegislationRegisterError(e);
                    }
                })
                .thenCompose(url -> downloadFile(url));

        return promise;

    }



    public static CompletableFuture<byte[]> downloadFile(URL url) {
        CompletableFuture<byte[]> promise = asyncHttpClient
                .prepareGet(url.toString())
                .execute()
                .toCompletableFuture()
                .thenApply(response -> response.getResponseBodyAsBytes());
        return promise;
    }

    public static CompletableFuture<String> downloadHtml(URL url) {
        CompletableFuture<String> promise = asyncHttpClient
                .prepareGet(url.toString())
                .execute()
                .toCompletableFuture()
                .thenApply(response -> response.getResponseBody());
        return promise;
    }

    public static CompletableFuture<URL> getRedirectTargetUrl(URL originalUrl) {
        assert (originalUrl.getHost().startsWith("www"));
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
                        URL redirectTarget = URI.create(String.format("%s://%s%s", originalUrl.getProtocol(), originalUrl.getHost(), redirectValue)).toURL();
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
        String cssSelector = String.format("a[title*=\"%s  authorised version\"], a[title*=\"%s authorised version\"]", registerID, registerID);
        Elements elements = htmlDocument.select(cssSelector);
        assert !elements.isEmpty();
        String linkUrl = elements.attr("href");
        assert !linkUrl.isEmpty();
        return URI.create(linkUrl).toURL();
    }

    public static URL getDocxDocumentLinkFromHtml(String html, String registerId) throws MalformedURLException {
        Document htmlDocument = Jsoup.parse(html);
        String cssSelector = String.format("a[title=\"%s\"]", registerId);
        Elements elements = htmlDocument.select(cssSelector);
        assert !elements.isEmpty();
        String linkUrl = elements.first().attr("href");
        assert !linkUrl.isEmpty();
        return URI.create(linkUrl).toURL();
    }


    public static Optional<String> getTitleStatus(String html) {

//        <li id="MainContent_ucLegItemPane_liStatus" class="info2">
//            <span id="MainContent_ucLegItemPane_lblTitleStatus" class="RedText">No longer in force</span>
//
//            <span id="MainContent_ucLegItemPane_lblVersionStatus" class="RedText"></span>
//        </li>

        return getCssIdValue(html, "MainContent_ucLegItemPane_lblTitleStatus");
    }


    public static Optional<String> getVersionStatus(String html) {

        return getCssIdValue(html, "MainContent_ucLegItemPane_lblVersionStatus");
    }

    @Override
    public CompletableFuture<Optional<String>> getRepealingRegisterId(String repealedRegisterId) {

        URL urlForSeriesPage = BuildUrl.forSeriesRepealedByPage(repealedRegisterId);

        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        CompletableFuture<Optional<String>> promise = asyncHttpClient
                .prepareGet(urlForSeriesPage.toString())
                .execute()
                .toCompletableFuture()
                .thenApply(response -> {
                    if (response.getStatusCode() == 200)
                    {
                        Optional<String> registerIdOfRepealingInstrument = getRegisterIdOfRepealedByCeasedBy(response.getResponseBody());
                        if (!registerIdOfRepealingInstrument.isPresent())
                        {
                            logger.trace("Did not find Register ID of repealing or ceasing instrument on this page: %n" + response.getResponseBody());
                            return Optional.empty();
                        }
                        return Optional.of(registerIdOfRepealingInstrument.get());
                    }
                    else {
                        logger.trace(String.format("Did not find Series page for Register ID: %s.%%nResponse Code: %d", repealedRegisterId, response.getStatusCode()));
                        return Optional.empty();
                    }
                });

        return promise;
    }

    public static Optional<String> getRegisterIdOfRepealedByCeasedBy(String html) {
        Document htmlDocument = Jsoup.parse(html);
        String cssSelector = String.format("a[id*='SeriesRepealedBy']");
        Elements elements = htmlDocument.select(cssSelector);
        if (elements.isEmpty())
        {
            return Optional.empty();
        }
        String linkUrl = elements.attr("href");

        if (linkUrl.isEmpty())
        {
            return Optional.empty();
        }
        Pattern pattern = Pattern.compile("(F[0-9]{4}[A-Z][0-9]+)");
        Matcher matcher = pattern.matcher(linkUrl);

        if (!matcher.find())
            return Optional.empty();

        String registerId = matcher.group(1);
        return Optional.of(registerId);
    }


    private static Optional<String> getCssIdValue(String html, String id) {
        Document htmlDocument = Jsoup.parse(html);
        String cssSelector = String.format("#%s", id);
        Elements elements = htmlDocument.select(cssSelector);
        if (elements.isEmpty()) {
            logger.error(String.format("Could not determine current status of instrument using selector '%s' from HTML: %n%s", cssSelector, html));
            return Optional.empty();
        }
        Element element = elements.first();
        String status = element.text();
        if (status.isEmpty()) {
            logger.error(String.format("Empty string value for status using selector '%s' from HTML: %n%s", cssSelector, html));
            return Optional.empty();
        }
        return Optional.of(status.trim());
    }

    private static class BuildUrl {
        public static URL toGetRedirect(String sourceRegisterId) {
            try {
                return new URL(String.format("%s/Latest/%s", BASE_URL, sourceRegisterId));
            } catch (MalformedURLException e) {
                throw new LegislationRegisterError(e);
            }
        }


        public static URL forLatestDownloadPage(String registerId) {
            try {
                return new URL(String.format("%s/Latest/%s/Download", BASE_URL, registerId));
            } catch (MalformedURLException e) {
                throw new LegislationRegisterError(e);
            }
        }

        public static URL forDownloadPage(String registerId) {
            try {
                return new URL(String.format("%s/Details/%s/Download",BASE_URL,registerId));
            } catch (MalformedURLException e)
            {
                throw new LegislationRegisterError(e);
            }


        }

        public static URL forSeriesRepealedByPage(String registerId)
        {
            try {
                return new URL(String.format("%s/Series/%s/RepealedBy",BASE_URL, registerId));
            } catch (MalformedURLException e) {
                throw new LegislationRegisterError(e);
            }
        }
    }

}
