import au.gov.dva.sopref.casesummary.CaseSummary;
import au.gov.dva.sopref.interfaces.model.Condition;
import au.gov.dva.sopref.interfaces.model.ServiceHistory;
import au.gov.dva.sopref.interfaces.model.SoP;
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


//    public CaseSummaryModelMock() {
//        _condition.setName("lumbar spondylosis");
//        _condition.setICDCode("ICD-10-AM");
//        _condition.setType("acute");
//        _condition.setOnsetStartDate(LocalDate.of(2008, 2, 18));
//
//        _serviceHistory.setEnlistmentDate(LocalDate.of(2006, 1, 10));
//        _serviceHistory.setSeparationDate(LocalDate.of(2015, 11, 23));
//        _serviceHistory.setHireDate(LocalDate.of(2006, 6, 28));
//
////        _serviceHistory.setServices();
////        _serviceHistory.setOperations();
//    }

//    @Test
//    public void ConditionNotEmpty() throws ExecutionException, InterruptedException {
//        Condition condition = new ConditionMock();
//        ServiceHistory serviceHistory = new ServiceHistoryMock();
//        SoP sop = new SoPMock();
//
//        CaseSummaryModel testData = new CaseSummaryModelMock(condition, serviceHistory, sop);
//        byte[] result = CaseSummary.createCaseSummary(testData).get();
//        Assert.assertTrue(result.length > 0);
//    }
}

