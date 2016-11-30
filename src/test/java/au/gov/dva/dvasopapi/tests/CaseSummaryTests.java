package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.CaseSummaryModelMock;
import au.gov.dva.sopref.casesummary.CaseSummary;
import au.gov.dva.sopref.casesummary.ChartGenerator;
import au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.script.ScriptException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;


public class CaseSummaryTests {

    @Test
    public void ResultNotEmpty() throws ExecutionException, InterruptedException {
        CaseSummaryModel testData = new CaseSummaryModelMock();
        byte[] result = CaseSummary.createCaseSummary(testData).get();
        Assert.assertTrue(result.length > 0);
    }

    @Test
    public void ResultSerialisesToWordDoc() throws ExecutionException,
            InterruptedException, IOException, URISyntaxException {

        CaseSummaryModel testData = new CaseSummaryModelMock();
        byte[] result = CaseSummary.createCaseSummary(testData).get();

        Path outputPath = getOutputPath();
        File outputFile = outputPath.toFile();

        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(result);
        outputStream.close();
        System.out.println("Wrote output to " + outputFile.getAbsolutePath());
        Assert.assertTrue(outputFile.exists());
    }

    private Path getOutputPath() throws URISyntaxException, IOException {
        String fileName = "Example Case Summary.docx";
        Path tempFilePath = Files.createTempFile("CaseSummaryTestOutput_",".docx");
        return tempFilePath;
    }

    //    @Test
//    public void GenerateCharts() throws FileNotFoundException, ScriptException {
//        ChartGenerator.generatePieChart();
//        Assert.assertTrue(5 > 0);
//    }

//    @Test
//    public void CompletableFutureIsComplete() {
//        CaseSummaryModel testData = new CaseSummaryModelMock();
//        byte[] result = CaseSummary.createCaseSummary(testData).get();
//        Assert.assertTrue(result.length > 0);
//    }

//    @Test
//    public void ConditionNotEmpty() throws ExecutionException, InterruptedException {
//        au.gov.dva.dvasopapi.tests.mocks.ConditionMock mockCondition = new au.gov.dva.dvasopapi.tests.mocks.ConditionMock();
//        ServiceHistory serviceHistory = new au.gov.dva.dvasopapi.tests.mocks.ServiceHistoryMock();
//        SoP sop = new au.gov.dva.dvasopapi.tests.mocks.SoPMock();
//
//        mockCondition.setName("Joint instability");
//        mockCondition.setICDCode("ICD-2017");
//        mockCondition.setType("Accumulated over time (wear and tear)");
//        mockCondition.setOnsetStartDate(LocalDate.of(2009, 12, 1));

//            try {
//        FileOutputStream outputStream = new FileOutputStream("C:\\Code\\DVA\\dva-sop-api\\src\\main\\resources\\docs\\Case Summary.docx");
//        outputStream.write(result);
//        outputStream.close();
//    } catch (IOException e) {
//
//    }
//
//        CaseSummaryModel testData = new au.gov.dva.dvasopapi.tests.mocks.CaseSummaryModelMock(mockCondition, null, null);
//        byte[] result = CaseSummary.createCaseSummary(testData).get();
//        Assert.assertTrue(result.length > 0);
//    }
}

