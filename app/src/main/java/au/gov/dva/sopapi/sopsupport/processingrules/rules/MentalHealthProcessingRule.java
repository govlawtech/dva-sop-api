package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class MentalHealthProcessingRule extends ProcessingRuleBase implements ProcessingRule  {

    private final IntervalSelector rhIntervalSelector;
    Interval rhInterval;

    public MentalHealthProcessingRule(ConditionConfiguration conditionConfiguration, IntervalSelector rhIntervalSelector) {
        super(conditionConfiguration);
        this.rhIntervalSelector = rhIntervalSelector;
    }

    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        if (super.shouldAbortProcessing(serviceHistory,condition,caseTrace))
        {
            return Optional.empty();
        }

        rhInterval = rhIntervalSelector.getInterval(serviceHistory,condition.getStartDate());
        return super.getApplicableSop(condition,serviceHistory,isOperational,rhInterval,true,caseTrace);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, ApplicableWearAndTearRuleConfiguration applicableWearAndTearRuleConfiguration, CaseTrace caseTrace) {

        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory,rhInterval, applicableWearAndTearRuleConfiguration,caseTrace);
    }

    @Override
    public void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        super.attachConfiguredFactorsToCaseTrace(condition,serviceHistory,caseTrace);
    }

}
