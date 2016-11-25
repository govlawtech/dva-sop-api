import au.gov.dva.sopref.casesummary.CaseSummary;
import au.gov.dva.sopref.interfaces.model.Condition;
import au.gov.dva.sopref.interfaces.model.ServiceHistory;
import au.gov.dva.sopref.interfaces.model.SoP;
import au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;


public class CaseSummaryTests {

    @Test
    public void ResultNotEmpty() throws ExecutionException, InterruptedException {
        CaseSummaryModel testData = new CaseSummaryModelMock();
        byte[] result = CaseSummary.createCaseSummary(testData).get();
        Assert.assertTrue(result.length > 0);
    }

    // TB todo: add further tests here

//    @Test
//    public void ConditionNotEmpty() throws ExecutionException, InterruptedException {
//        ConditionMock mockCondition = new ConditionMock();
//        ServiceHistory serviceHistory = new ServiceHistoryMock();
//        SoP sop = new SoPMock();
//
//        mockCondition.setName("Joint instability");
//        mockCondition.setICDCode("ICD-2017");
//        mockCondition.setType("Accumulated over time (wear and tear)");
//        mockCondition.setOnsetStartDate(LocalDate.of(2009, 12, 1));
//
//        CaseSummaryModel testData = new CaseSummaryModelMock(mockCondition, null, null);
//        byte[] result = CaseSummary.createCaseSummary(testData).get();
//        Assert.assertTrue(result.length > 0);
//    }
}

