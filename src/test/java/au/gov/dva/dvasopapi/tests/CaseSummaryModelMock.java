package au.gov.dva.dvasopapi.tests;

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

    public Condition getCondition() {
        return _condition;
    }

    public void setCondition(Condition condition) {
        _condition = condition;
    }

    public ServiceHistory getServiceHistory() {
        return _serviceHistory;
    }

    public void setServiceHistory(ServiceHistory serviceHistory) {
        _serviceHistory = serviceHistory;
    }

    public SoP getSop() {
        return _sop;
    }

    public void setSop(SoP sop) {
        _sop = sop;
    }
}
