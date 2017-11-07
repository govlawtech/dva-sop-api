package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class GenericProcessingRuleWithUniqueTestPeriodsForRHandBoP extends ProcessingRuleBase implements ProcessingRule {

    private final IntervalSelector intervalInWhichToCountOperationalServiceDays;
    private final IntervalSelector rhSelector;
    private final IntervalSelector bopSelector;

    public GenericProcessingRuleWithUniqueTestPeriodsForRHandBoP(ConditionConfiguration conditionConfiguration,
                                                                 IntervalSelector intervalInWhichToCountOperationalServiceDays,
                                                                 IntervalSelector rhSelector,
                                                                 IntervalSelector bopSelector) {
        super(conditionConfiguration);
        this.intervalInWhichToCountOperationalServiceDays = intervalInWhichToCountOperationalServiceDays;
        this.rhSelector = rhSelector;
        this.bopSelector = bopSelector;
    }

    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        if (super.shouldAbortProcessing(serviceHistory,condition,caseTrace)) {
            return Optional.empty();
        }

        Interval testInterval = intervalInWhichToCountOperationalServiceDays.getInterval(serviceHistory,condition.getStartDate());
        return super.getApplicableSop(condition,serviceHistory,isOperational,testInterval,caseTrace);

    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        ApplicableRuleConfiguration applicableRuleConfiguration = super.getApplicableRuleConfiguration(serviceHistory,condition,caseTrace).get();
        Optional<? extends RuleConfigurationItem> applicableRuleConfigurationItem = applicableRuleConfiguration.getRuleConfigurationForStandardOfProof(applicableSop.getStandardOfProof());

        Interval testIntervalForCFTSdays = applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis ?
                rhSelector.getInterval(serviceHistory,condition.getStartDate()) :
                bopSelector.getInterval(serviceHistory,condition.getStartDate());

        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory,testIntervalForCFTSdays,applicableRuleConfigurationItem,caseTrace);
    }

    @Override
    public void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        super.attachConfiguredFactorsToCaseTrace(condition,serviceHistory,caseTrace);
    }
}
