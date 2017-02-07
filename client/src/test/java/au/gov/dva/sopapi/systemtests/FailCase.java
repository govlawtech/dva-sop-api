package au.gov.dva.sopapi.systemtests;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class FailCase {

    @Test
    public void runSystemTestCaseWhereInsufficientCfts() throws IOException {
        TestCaseResult result = DvaDefinedTest.runTestCase("FAIL_3.json");
        System.out.println(result.log);
        Assert.assertTrue(result.passed);
    }

    @Test
    public void systemTestCaseWhereInjuryOccursBeforeService() {
        TestCaseResult result = DvaDefinedTest.runTestCase("FAIL_5.json");
        System.out.println(result.log);
        Assert.assertTrue(result.passed);
    }
}
