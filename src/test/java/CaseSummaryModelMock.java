import au.gov.dva.sopref.interfaces.model.Condition;
import au.gov.dva.sopref.interfaces.model.ServiceHistory;
import au.gov.dva.sopref.interfaces.model.SoP;
import au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel;

import java.time.LocalDate;

public class CaseSummaryModelMock implements CaseSummaryModel{

    // TB todo: make a test implementation here

    private ConditionMock _condition = new ConditionMock();
    private ServiceHistoryMock _serviceHistory;
    private SoP _sop;

    public CaseSummaryModelMock() {
        _condition.setName("lumbar spondylosis");
        _condition.setICDCode("ICD-10-AM");
        _condition.setType("acute");
        _condition.setOnsetStartDate(LocalDate.of(2008, 2, 18));

        _serviceHistory.setEnlistmentDate(LocalDate.of(2006, 1, 10));
        _serviceHistory.setSeparationDate(LocalDate.of(2015, 11, 23));
        _serviceHistory.setHireDate(LocalDate.of(2006, 6, 28));

//        _serviceHistory.setServices();
//        _serviceHistory.setOperations();
    }

    public Condition getCondition() {
        return _condition;
    }

    public ServiceHistory getServiceHistory() {
        return _serviceHistory;
    }

    public SoP getSop() {
        return _sop;
    }
}
