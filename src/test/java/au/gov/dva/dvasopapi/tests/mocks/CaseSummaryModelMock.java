package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopref.interfaces.model.Condition;
import au.gov.dva.sopref.interfaces.model.ServiceHistory;
import au.gov.dva.sopref.interfaces.model.SoP;
import au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel;

public class CaseSummaryModelMock implements CaseSummaryModel{

    public Condition getCondition() {
        return new ConditionMock();
    }

    public ServiceHistory getServiceHistory() {
        return new ServiceHistoryMock();
    }

    public SoP getSop() {
        return new MockLumbarSpondylosisSop();
    }
}
