package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.FixedDaysPeriodSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.FixedYearsPeriodSelector;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class InvertebralDiscProlapseRule extends ProcessingRuleBase implements ProcessingRule {

    public InvertebralDiscProlapseRule(ConditionConfiguration conditionConfiguration) {
        super(conditionConfiguration);
    }

    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        if (super.shouldAbortProcessing(serviceHistory,condition,caseTrace))
        {
            return Optional.empty();
        }

        IntervalSelector intervalSelector = new FixedYearsPeriodSelector(10);
        Interval testInterval = intervalSelector.getInterval(serviceHistory,condition.getStartDate());
        return super.getApplicableSop(condition,serviceHistory,isOperational,testInterval,caseTrace);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {

        IntervalSelector intervalSelector =  applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis ? new FixedYearsPeriodSelector(10) : new FixedYearsPeriodSelector(5);
        Interval testInterval = intervalSelector.getInterval(serviceHistory,condition.getStartDate());
        Optional<? extends RuleConfigurationItem> applicableRuleConfigurationItem  = this.getApplicableRuleConfiguration(serviceHistory,condition,caseTrace)
                .get().getRuleConfigurationForStandardOfProof(applicableSop.getStandardOfProof());

        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory,testInterval,applicableRuleConfigurationItem,caseTrace);
    }

    @Override
    public void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        super.attachConfiguredFactorsToCaseTrace(condition,serviceHistory,caseTrace);
    }
}
