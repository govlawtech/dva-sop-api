package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class GenericProcessingRule extends ProcessingRuleBase implements ProcessingRule {
    private final IntervalSelector _intervalSelector;

    public GenericProcessingRule(ConditionConfiguration conditionConfiguration, IntervalSelector intervalSelectorForBothRHandBop) {
        super(conditionConfiguration);
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
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        ApplicableRuleConfiguration applicableRuleConfiguration = super.getApplicableRuleConfiguration(serviceHistory,condition,caseTrace).get();
        Optional<? extends RuleConfigurationItem> applicableRuleConfigurationItem = applicableRuleConfiguration.getRuleConfigurationForStandardOfProof(applicableSop.getStandardOfProof());
        Interval testInterval = _intervalSelector.getInterval(serviceHistory,condition.getStartDate());
        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory,testInterval,applicableRuleConfigurationItem,caseTrace);
    }

    @Override
    public void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        super.attachConfiguredFactorsToCaseTrace(condition,serviceHistory,caseTrace);
    }
}



