import au.gov.dva.sopref.data.FederalRegisterOfLegislation;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class LegislationRegisterTests {

    @Test
    public void extractAuthorizedDownloadLinkFromHtml() throws IOException {
        // Source: https://www.legislation.gov.au/Details/F2014L00930/Download
        URL htmlResource = Resources.getResource("lsDownloadPage.html");
        String rawHtml = Resources.toString(htmlResource, Charsets.UTF_8);
        URL result = FederalRegisterOfLegislation.getAuthorisedDocumentLinkFromHtml(rawHtml,"F2014L00930");
        Assert.assertTrue(result.equals(URI.create("https://www.legislation.gov.au/Details/F2014L00930/d88e6f5d-a696-4e2f-8b68-cf8a56acfdd6").toURL()));
    }
}
