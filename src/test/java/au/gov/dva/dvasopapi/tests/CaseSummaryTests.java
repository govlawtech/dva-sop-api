package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.CaseSummaryModelMock;
import au.gov.dva.sopref.casesummary.CaseSummary;
import au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;


public class CaseSummaryTests {

    @Test
    public void resultNotEmpty() throws ExecutionException, InterruptedException {
        CaseSummaryModel testData = new CaseSummaryModelMock();
        byte[] result = CaseSummary.createCaseSummary(testData).get();
        Assert.assertTrue(result.length > 0);
    }

    @Test
    public void resultSerialisesToWordDoc() throws ExecutionException,
            InterruptedException, IOException, URISyntaxException {

        CaseSummaryModel testData = new CaseSummaryModelMock();
        byte[] result = CaseSummary.createCaseSummary(testData).get();

        Path outputPath = getOutputPath();
        File outputFile = outputPath.toFile();

        try (FileOutputStream outputStream = new FileOutputStream(outputFile);) {
            outputStream.write(result);
            outputStream.close();
        }

        Assert.assertTrue(outputFile.exists());
    }

    private Path getOutputPath() throws IOException {
        Path tempFilePath = Files.createTempFile("CaseSummaryTestOutput_",".docx");
        return tempFilePath;
    }
}

