import au.gov.dva.sopref.casesummary.CaseSummary;
import au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;


public class CaseSummaryTests {

    @Test
    public void ResultNotEmpty() throws ExecutionException, InterruptedException {
        CaseSummaryModel testData = new CaseSummaryModelMock();
        byte[] result = CaseSummary.createCaseSummary(testData).get();
        Assert.assertTrue(result.length > 0);
    }


    // TB todo: add further tests here
}

