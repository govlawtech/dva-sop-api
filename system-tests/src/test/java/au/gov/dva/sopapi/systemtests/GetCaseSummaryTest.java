package au.gov.dva.sopapi.systemtests;

import au.gov.dva.sopapi.AppSettings;
import au.gov.dva.sopapi.client.SoPApiClient;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.FactorWithInferredResultDto;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Created by michael carter on 10/03/2017.
 */
public class GetCaseSummaryTest {
    private static final String TEST_FILE_DIR = "dvaDefinedTestData";
    private static String testData;

    @BeforeClass
    public static void beforeClassMethod() throws IOException{
//        org.junit.Assume.assumeTrue(AppSettings.isEnvironmentSet() && AppSettings.getEnvironment() == AppSettings.Environment.devtest);

        // Get test data
        ResourceDirectoryLoader resourceDirectoryLoader = new ResourceDirectoryLoader();
        testData = Resources.toString(Resources.getResource(TEST_FILE_DIR + "/PASS_1.json"), Charsets.UTF_8);
    }

    @Test
    public void getCaseSummaryTest() throws IOException, ExecutionException, InterruptedException {

            URL url = new URL(AppSettings.getBaseUrl());
            SoPApiClient underTest = new SoPApiClient(url, Optional.empty());
            byte[] response = underTest.getCaseSummary(testData).get();

            // Write results locally
            Path tempFilePath = Files.createTempFile("CaseSummaryTestOutput_", ".docx");
            File outputFile = tempFilePath.toFile();
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(response);
                outputStream.close();
            }
            System.out.println(outputFile.getAbsolutePath());

    }
}
