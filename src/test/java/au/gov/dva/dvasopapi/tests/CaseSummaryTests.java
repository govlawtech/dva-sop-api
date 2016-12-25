package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.CaseSummaryModelMock;
import au.gov.dva.sopapi.interfaces.model.ServiceType;
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
import java.util.function.Function;


public class CaseSummaryTests {

    Function<String,ServiceType> mockCat = s -> {
        if (s.contains("SLIPPER"))
            return ServiceType.WARLIKE;
        if (s.contains("HARWICK"))
            return ServiceType.NON_WARLIKE;

        else return ServiceType.PEACETIME;
    };

    @Test
    public void resultNotEmpty() throws ExecutionException, InterruptedException {
        CaseSummaryModel testData = new CaseSummaryModelMock();



        byte[] result = CaseSummary.createCaseSummary(testData,mockCat).get();
        Assert.assertTrue(result.length > 0);
    }

    @Test
    public void resultSerialisesToWordDoc() throws ExecutionException,
            InterruptedException, IOException, URISyntaxException {

        CaseSummaryModel testData = new CaseSummaryModelMock();
        byte[] result = CaseSummary.createCaseSummary(testData, mockCat).get();

        Path outputPath = getOutputPath();
        File outputFile = outputPath.toFile();

        try (FileOutputStream outputStream = new FileOutputStream(outputFile);) {
            outputStream.write(result);
            outputStream.close();
        }

        System.out.println(outputFile.getAbsolutePath());


        Assert.assertTrue(outputFile.exists());
    }

    private Path getOutputPath() throws IOException {
        Path tempFilePath = Files.createTempFile("CaseSummaryTestOutput_",".docx");
        return tempFilePath;
    }
}

