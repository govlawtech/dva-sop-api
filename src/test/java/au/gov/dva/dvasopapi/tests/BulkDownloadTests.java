package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import au.gov.dva.sopref.data.Conversions;
import au.gov.dva.sopref.data.FederalRegisterOfLegislation;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class BulkDownloadTests {

    @Ignore
    @Category(IntegrationTest.class)
    @Test
    public void bulkSoPDownload() throws IOException, URISyntaxException, ExecutionException, InterruptedException {

        URL rhRegisterIds = Resources.getResource("rhSopRegisterIds.txt");
        String[] ids = Resources.toString(rhRegisterIds, Charset.forName("UTF-8")).split("[\r\n]+");

        FederalRegisterOfLegislation client = new FederalRegisterOfLegislation();
        for (String id : ids) {
            Path targetPath = getOutputPath(id);
            byte[] pdf = client.getAuthorisedInstrumentPdf(id).get();
            Files.write(targetPath,pdf);
            System.out.print(targetPath);
        }
    }

    private Path getOutputPath(String id) throws URISyntaxException, IOException {
        Path tempFilePath = Files.createTempFile(id + "_", ".docx");
        return tempFilePath;
    }
}
