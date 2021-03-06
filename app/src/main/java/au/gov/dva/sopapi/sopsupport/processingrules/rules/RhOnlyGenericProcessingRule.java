package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.interfaces.ApplicableWearAndTearRuleConfiguration;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.ConditionConfiguration;
import au.gov.dva.sopapi.interfaces.IntervalSelector;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class RhOnlyGenericProcessingRule extends GenericWearAndTearProcessingRule {

    private final IntervalSelector _intervalSelector;

    public RhOnlyGenericProcessingRule(ApplicableWearAndTearRuleConfiguration applicableWearAndTearRuleConfiguration, IntervalSelector intervalSelectorForRh)
    {
        super(applicableWearAndTearRuleConfiguration,intervalSelectorForRh);
        _intervalSelector = intervalSelectorForRh;

    }

    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        if (super.shouldAbortProcessing(serviceHistory,condition,caseTrace))
        {
            return Optional.empty();
        }
        Interval rhIntervalUsed = _intervalSelector.getInterval(serviceHistory,condition.getStartDate());
        return super.getApplicableSop(condition,serviceHistory,isOperational,rhIntervalUsed,true,caseTrace);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory,  CaseTrace caseTrace) {
        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory,caseTrace);
    }


}
