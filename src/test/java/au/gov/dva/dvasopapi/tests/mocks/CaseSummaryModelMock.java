import au.gov.dva.sopref.interfaces.model.Condition;
import au.gov.dva.sopref.interfaces.model.ServiceHistory;
import au.gov.dva.sopref.interfaces.model.SoP;
import au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel;

public class CaseSummaryModelMock implements CaseSummaryModel{

    // TB todo: make a test implementation here

    private Condition _condition;
    private ServiceHistory _serviceHistory;
    private SoP _sop;

    public CaseSummaryModelMock() {
        
    }

    public CaseSummaryModelMock(Condition condition, ServiceHistory serviceHistory, SoP sop) {
        _condition = condition;
        _serviceHistory = serviceHistory;
        _sop = sop;
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
