package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.time.OffsetDateTime;


public class OnsetCondition implements Condition {

    private final SoPPair soPPair;
    private final LocalDate onsetStartDate;
    private final LocalDate onsetEndDate;
    private ProcessingRule processingRule;

    public OnsetCondition(SoPPair soPPair, LocalDate onsetStartDate, LocalDate onsetEndDate, ProcessingRule processingRule)
    {
        this.soPPair = soPPair;
        this.onsetStartDate = onsetStartDate;
        this.onsetEndDate = onsetEndDate;
        this.processingRule = processingRule;
    }

    @Override
    public SoPPair getSopPair() {
        return soPPair;
    }

    @Override
    public LocalDate getStartDate() {
        return onsetStartDate;
    }

    @Override
    public LocalDate getEndDate() {
        return onsetEndDate;
    }

    @Override
    public ProcessingRule getProcessingRule() {
        return processingRule;
    }

    @Override
    public ImmutableList<Factor> getApplicableFactors(SoP sop) {
        return sop.getOnsetFactors();
    }


}
