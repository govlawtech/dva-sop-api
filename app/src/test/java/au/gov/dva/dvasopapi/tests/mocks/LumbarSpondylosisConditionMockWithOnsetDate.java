package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;

public class LumbarSpondylosisConditionMockWithOnsetDate implements Condition
{
    private final LocalDate onsetDate;

    public LumbarSpondylosisConditionMockWithOnsetDate(LocalDate onsetDate)
    {
        this.onsetDate = onsetDate;
    }

    @Override
    public SoPPair getSopPair() {
         return new SoPPair("lumbar spondylosis", new MockLumbarSpondylosisSopBoP(),new MockLumbarSpondylosisSopRH());
    }

    @Override
    public LocalDate getStartDate() {
        return onsetDate;
    }

    @Override
    public LocalDate getEndDate() {
        return onsetDate;
    }

    @Override
    public ProcessingRule getProcessingRule() {
        return null;
    }

    @Override
    public ImmutableList<Factor> getApplicableFactors(SoP sop) {
        return null;
    }
}
