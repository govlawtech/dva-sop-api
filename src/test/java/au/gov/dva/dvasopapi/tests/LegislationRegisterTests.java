package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import au.gov.dva.dvasopapi.tests.categories.IntegrationTestImpl;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.sopref.data.updates.LegRegChangeDetector;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class LegislationRegisterTests extends IntegrationTestImpl {

    @Test
    public void extractAuthorizedDownloadLinkFromHtml() throws IOException {
        // Source: https://www.legislation.gov.au/Details/F2014L00930/Download
        URL htmlResource = Resources.getResource("lsDownloadPage.html");
        String rawHtml = Resources.toString(htmlResource, Charsets.UTF_8);
        URL result = FederalRegisterOfLegislationClient.getAuthorisedDocumentLinkFromHtml(rawHtml,"F2014L00930");
        Assert.assertTrue(result.equals(URI.create("https://www.legislation.gov.au/Details/F2014L00930/d88e6f5d-a696-4e2f-8b68-cf8a56acfdd6").toURL()));
    }

    @Category(IntegrationTest.class)
    @Test
    public void directDownloadPdf() throws ExecutionException, InterruptedException, IOException {
        URL testURL = URI.create("https://www.legislation.gov.au/Details/F2014L00930/d88e6f5d-a696-4e2f-8b68-cf8a56acfdd6").toURL();
        byte[] result = new FederalRegisterOfLegislationClient().downloadFile(testURL).get();
        Assert.assertTrue( result.length == 391904);
    }

    @Category(IntegrationTest.class)
    @Test
    public void getRedirectForDetails() throws MalformedURLException, ExecutionException, InterruptedException {
        URL testInstrumentPage = URI.create("https://www.legislation.gov.au/Latest/F2014L00930/Download").toURL();
        URL result = FederalRegisterOfLegislationClient.getRedirectTargetUrl(testInstrumentPage).get();
        Assert.assertTrue(result.equals(URI.create("https://www.legislation.gov.au/Details/F2014L00930/Download").toURL()));
    }

    @Category(IntegrationTest.class)
    @Test
    public void endToEndPdfRetrieval() throws ExecutionException, InterruptedException {
        String testRegisterId = "F2014L00930";
        FederalRegisterOfLegislationClient underTest = new FederalRegisterOfLegislationClient();
        byte[] result = underTest.getLatestAuthorisedInstrumentPdf(testRegisterId).get();
        Assert.assertTrue(result.length == 391904);
    }

    @Test
    public void noLongerInForceStatusTextRetrieved() throws IOException {

        // Statement of Principles concerning animal envenomation No. 67 of 2008 (https://www.legislation.gov.au/Series/F2008L03183) was revoked by
        // Statement of Principles concerning animal envenomation (Balance of Probabilities) (No. 82 of 2016) (https://www.legislation.gov.au/Details/F2016L01666)
        // https://www.legislation.gov.au/Series/F2008L03183/RepealedBy lists the repealing SoP, including its RegisterId

        Optional<String> result = FederalRegisterOfLegislationClient.getTitleStatus(Resources.toString(Resources.getResource("legislationRegister/ceasedNoLongerInForce.html"),Charsets.UTF_8));
        String expectedStatus =  "No longer in force";
        Assert.assertTrue(result.get().contentEquals(expectedStatus));
    }

//       <li id="MainContent_ucLegItemPane_liStatus" class="info2">
//            <span id="MainContent_ucLegItemPane_lblTitleStatus" class="RedText">In force</span>
//            <span id="MainContent_ucLegItemPane_lblStatusSeperator" class="RedText"> - </span>
//            <span id="MainContent_ucLegItemPane_lblVersionStatus" class="RedText">Superseded Version</span>
//        </li>

    @Test
    public void inForceLatest() throws IOException {

    Optional<String> result = FederalRegisterOfLegislationClient.getTitleStatus(Resources.toString(Resources.getResource("legislationRegister/inForceLatest.html"),Charsets.UTF_8));
        String expectedStatus =  "In force";
        Assert.assertTrue(result.get().contentEquals(expectedStatus));
    }

    @Test
    public void inForceSuperceded() throws IOException {

        Optional<String> result = FederalRegisterOfLegislationClient.getTitleStatus(Resources.toString(Resources.getResource("legislationRegister/inForceSuperceded.html"),Charsets.UTF_8));
        String expectedStatus =  "In force";
        Assert.assertTrue(result.get().contentEquals(expectedStatus));
    }

    @Test
    public void getVersionStatus() throws IOException {
        // when there is a later compilation
        Optional<String> result = FederalRegisterOfLegislationClient.getVersionStatus(Resources.toString(Resources.getResource("legislationRegister/inForceSuperceded.html"),Charsets.UTF_8));
        String expectedStatus =  "Superseded Version";
        Assert.assertTrue(result.get().contentEquals(expectedStatus));
    }

    @Test
    public void getUrlOfRepealedCeasedBy() throws IOException {
        Optional<String> result = FederalRegisterOfLegislationClient.getRegisterIdOfRepealedByCeasedBy(Resources.toString(Resources.getResource("legislationRegister/seriesRepealedBy.html"),Charsets.UTF_8));
        Assert.assertTrue(result.get().contentEquals("F2016L01667"));
    }


    @Test
    public void detectNewCompliationsInBulk() throws IOException {
        LegRegChangeDetector underTest = new LegRegChangeDetector(new FederalRegisterOfLegislationClient());
        String[] toTest = Resources.toString(Resources.getResource("rhSopRegisterIds.txt"), Charsets.UTF_8).split(System.getProperty("line.separator"));
        ImmutableSet<InstrumentChange> results =  underTest.detectReplacements(ImmutableSet.copyOf(toTest));
        System.out.println(results.size());
        assert(results.size() == 2);
    }


}
