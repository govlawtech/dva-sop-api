package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class ConditionMock implements Condition
{

    private final SoPPair soPPair;
    private final LocalDate start;
    private final LocalDate end;
    private final ProcessingRule processingRule;

    public ConditionMock(SoPPair soPPair, LocalDate start, LocalDate end, ProcessingRule processingRule) {
        this.soPPair = soPPair;
        this.start = start;
        this.end = end;
        this.processingRule = processingRule;
    }

    @Override
    public SoPPair getSopPair() {
        return soPPair;
    }

    @Override
    public LocalDate getStartDate() {
        return start;
    }

    @Override
    public LocalDate getEndDate() {
        return end;
    }

    @Override
    public ProcessingRule getProcessingRule() {
        return processingRule;
    }

    @Override
    public ImmutableList<Factor> getApplicableFactors(SoP sop) {
        return null;
    }

}
