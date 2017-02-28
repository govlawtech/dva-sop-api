package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.dvasopapi.tests.mocks.processingRules.SimpleServiceHistory;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;

public class SimpleCaseSummaryModelMock extends ExtensiveCaseSummaryModelMock {


    @Override
    public ServiceHistory getServiceHistory() {
        return SimpleServiceHistory.get();
    }
}
