package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class GenericWearAndTearProcessingRule extends ProcessingRuleBase implements WearAndTearProcessingRule  {
    private final IntervalSelector _intervalSelector;

    public GenericWearAndTearProcessingRule(ApplicableWearAndTearRuleConfiguration applicableWearAndTearRuleConfiguration, IntervalSelector intervalSelectorForBothRHandBop) {
        super(applicableWearAndTearRuleConfiguration);
        _intervalSelector = intervalSelectorForBothRHandBop;
    }

    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        if (super.shouldAbortProcessing(serviceHistory,condition,caseTrace))
        {
            return Optional.empty();
        }

        Interval rhIntervalUsed = _intervalSelector.getInterval(serviceHistory,condition.getStartDate());
        return super.getApplicableSop(condition,serviceHistory,isOperational,rhIntervalUsed,caseTrace);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory,  CaseTrace caseTrace) {

        Interval testInterval = _intervalSelector.getInterval(serviceHistory,condition.getStartDate());
        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory,testInterval, applicableWearAndTearRuleConfiguration,caseTrace);
    }


}



