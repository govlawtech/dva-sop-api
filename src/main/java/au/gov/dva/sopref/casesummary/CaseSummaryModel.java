package au.gov.dva.sopref.casesummary;

import au.gov.dva.sopref.interfaces.model.Condition;
import au.gov.dva.sopref.interfaces.model.ServiceHistory;
import au.gov.dva.sopref.interfaces.model.SoP;

public class CaseSummaryModel implements au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel {

    private Condition _condition;
    private ServiceHistory _serviceHistory;
    private SoP _sop;

    public CaseSummaryModel() {

    }

    public CaseSummaryModel(Condition condition, ServiceHistory serviceHistory, SoP sop) {
        _condition = condition;
        _serviceHistory = serviceHistory;
        _sop = sop;
    }

    @Override
    public Condition getCondition() {
        return _condition;
    }

    @Override
    public ServiceHistory getServiceHistory() {
        return _serviceHistory;
    }

    @Override
    public SoP getSop() {
        return _sop;
    }
}
