package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.ExtensiveCaseSummaryModelMock;
import au.gov.dva.dvasopapi.tests.mocks.LumbarSpondylosisConditionMock;
import au.gov.dva.dvasopapi.tests.mocks.MockLumbarSpondylosisSopRH;
import au.gov.dva.dvasopapi.tests.mocks.SimpleCaseSummaryModelMock;
import au.gov.dva.dvasopapi.tests.mocks.processingRules.SimpleServiceHistory;
import au.gov.dva.sopapi.interfaces.model.*;

import au.gov.dva.sopapi.interfaces.model.casesummary.CaseSummaryModel;
import au.gov.dva.sopapi.sopsupport.casesummary.CaseSummary;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
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


        byte[] result = CaseSummary.createCaseSummary(testData, mockCat).get();
        Assert.assertTrue(result.length > 0);
    }

    @Test
    public void resultSerialisesToWordDoc() throws ExecutionException,
            InterruptedException, IOException, URISyntaxException {

        CaseSummaryModel testData = new ExtensiveCaseSummaryModelMock();
        byte[] result = CaseSummary.createCaseSummary(testData, mockCat).get();

        File resultFile = writeTempFile(result);
        System.out.println(resultFile.getAbsolutePath());

        Assert.assertTrue(resultFile.exists());
    }

    @Test
    public void caseSummaryWithSimpleServiceHistory() throws ExecutionException, InterruptedException, IOException {
        CaseSummaryModel simpleCaseSummaryModel = new SimpleCaseSummaryModelMock();

        // expected results from looking at mock data:
        // peacetime service should be 31
        // operational service should be 122
        byte[] result = CaseSummary.createCaseSummary(simpleCaseSummaryModel,mockCat).get();
        File resultFile = writeTempFile(result);
        System.out.println(resultFile.getAbsolutePath());
        Assert.assertTrue(resultFile.exists());

    }

    private File writeTempFile(byte[] data) throws IOException {
         Path outputPath = getOutputPath();
        File outputFile = outputPath.toFile();

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(data);
            outputStream.close();
        }

        return outputFile;

    }

    private Path getOutputPath() throws IOException {
        Path tempFilePath = Files.createTempFile("CaseSummaryTestOutput_", ".docx");
        return tempFilePath;
    }

}

