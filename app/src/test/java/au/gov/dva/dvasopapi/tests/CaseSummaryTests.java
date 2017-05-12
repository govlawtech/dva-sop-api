package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.ExtensiveCaseSummaryModelMock;
import au.gov.dva.dvasopapi.tests.mocks.SimpleCaseSummaryModelMock;
import au.gov.dva.sopapi.interfaces.model.*;

import au.gov.dva.sopapi.interfaces.model.casesummary.CaseSummaryModel;
import au.gov.dva.sopapi.sopsupport.casesummary.CaseSummary;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;


public class CaseSummaryTests {

    Predicate<Deployment> mockCat = s -> {
        if (s.getOperationName().contains("Peace is Our Profession"))
            return false;
        else return true;
    };

    @Test
    public void resultNotEmpty() throws ExecutionException, InterruptedException {
        CaseSummaryModel testData = new ExtensiveCaseSummaryModelMock();


        byte[] result = CaseSummary.createCaseSummary(testData, mockCat, false).get();
        Assert.assertTrue(result.length > 0);
    }

    @Test
    public void resultSerialisesToWordDoc() throws ExecutionException,
            InterruptedException, IOException, URISyntaxException {

        CaseSummaryModel testData = new ExtensiveCaseSummaryModelMock();
        byte[] result = CaseSummary.createCaseSummary(testData, mockCat, false).get();

        File resultFile = writeTempFile(result, false);
        System.out.println(resultFile.getAbsolutePath());

        Assert.assertTrue(resultFile.exists());
    }

    @Test
    public void resultSerialisesToPdfDoc() throws ExecutionException,
            InterruptedException, IOException, URISyntaxException {

        CaseSummaryModel testData = new ExtensiveCaseSummaryModelMock();
        byte[] result = CaseSummary.createCaseSummary(testData, mockCat, true).get();

        File resultFile = writeTempFile(result, true);
        System.out.println(resultFile.getAbsolutePath());

        Assert.assertTrue(resultFile.exists());
    }

    @Test
    public void caseSummaryWithSimpleServiceHistory() throws ExecutionException, InterruptedException, IOException {
        CaseSummaryModel simpleCaseSummaryModel = new SimpleCaseSummaryModelMock();

        // expected results from looking at mock data:
        // peacetime service should be 31
        // operational service should be 122
        byte[] result = CaseSummary.createCaseSummary(simpleCaseSummaryModel,mockCat, false).get();
        File resultFile = writeTempFile(result, false);
        System.out.println(resultFile.getAbsolutePath());
        Assert.assertTrue(resultFile.exists());

    }

    private File writeTempFile(byte[] data, boolean usePdfExtension) throws IOException {
         Path outputPath = getOutputPath(usePdfExtension);
        File outputFile = outputPath.toFile();

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(data);
            outputStream.close();
        }

        return outputFile;

    }

    private Path getOutputPath(boolean usePdfExtension) throws IOException {
        Path tempFilePath = Files.createTempFile("CaseSummaryTestOutput_"
                , usePdfExtension ? ".pdf" : ".docx");
        return tempFilePath;
    }

}

