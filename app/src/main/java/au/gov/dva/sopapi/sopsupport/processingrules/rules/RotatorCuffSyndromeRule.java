package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.FixedDaysPeriodSelector;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class RotatorCuffSyndromeRule extends ProcessingRuleBase implements WearAndTearProcessingRule {

    Interval rhIntervalUsed;

    public RotatorCuffSyndromeRule(ApplicableWearAndTearRuleConfiguration applicableWearAndTearRuleConfiguration) {
        super(applicableWearAndTearRuleConfiguration);
    }

    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        if (super.shouldAbortProcessing(serviceHistory,condition,caseTrace))
        {
            return Optional.empty();
        }

        IntervalSelector rhIntervalSelector = new FixedDaysPeriodSelector(150); // para (b)(ii): 120 + 30 days
        Interval testInterval = rhIntervalSelector.getInterval(serviceHistory,condition.getStartDate());
        rhIntervalUsed = testInterval;
        return super.getApplicableSop(condition,serviceHistory,isOperational,testInterval,caseTrace);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory,  CaseTrace caseTrace) {
        if (rhIntervalUsed == null)
        {
            throw new IllegalStateException("Must determine applicable SoP before getting satisfied factors");
        }

        Interval testInterval = applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis ?
                rhIntervalUsed : new FixedDaysPeriodSelector(240).getInterval(serviceHistory,condition.getStartDate());



        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory,testInterval, applicableWearAndTearRuleConfiguration,caseTrace);
    }


}
