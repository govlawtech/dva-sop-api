package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.SoPPair;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ConditionMock implements Condition {

    @Override
    public SoPPair getSopPair() {
        return new SoPPair(null,new MockLumbarSpondylosisSop());
    }

    public OffsetDateTime getStartDate() {
        return OffsetDateTime.of(2004,11,1,0,0,0,0,ZoneOffset.UTC);
    }

    public OffsetDateTime getEndDate() {
        return OffsetDateTime.of(2004, 11, 7,0,0,0,0,ZoneOffset.UTC);
    }

    @Override
    public ProcessingRule getProcessingRule() {
        return null;
    }

}