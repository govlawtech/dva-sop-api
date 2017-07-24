package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.*;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.SlidingCFTSSelectorWithYearLimit;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.SlidingOperationalServicePeriodSelector;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class LumbarSpondylosisRule extends ProcessingRuleBase implements ProcessingRule {

    Interval rhIntervalUsed;

    public LumbarSpondylosisRule(ConditionConfiguration conditionConfiguration) {
        super(conditionConfiguration);
    }

    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        if (super.shouldAbortProcessing(serviceHistory,condition,caseTrace))
        {
            return Optional.empty();
        }

        IntervalSelector intervalSelector = new SlidingOperationalServicePeriodSelector(isOperational,10);
        Interval intervalForTestingRh = intervalSelector.getInterval(serviceHistory,condition.getStartDate());
        rhIntervalUsed = intervalForTestingRh;
        return super.getApplicableSop(condition,serviceHistory,isOperational,intervalForTestingRh,caseTrace);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {

        if (rhIntervalUsed == null)
        {
            throw new IllegalStateException("Must determine applicable SoP before getting satisfied factors");
        }
        Interval testInterval = applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis ?
                rhIntervalUsed : (new SlidingCFTSSelectorWithYearLimit( 10,25).getInterval(serviceHistory,condition.getStartDate()));

        ApplicableRuleConfiguration applicableRuleConfiguration = super.getApplicableRuleConfiguration(serviceHistory,condition,caseTrace).get();
        Optional<? extends  RuleConfigurationItem> applicableRuleConfigurationItem = applicableRuleConfiguration.getRuleConfigurationForStandardOfProof(applicableSop.getStandardOfProof());


        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory,testInterval,applicableRuleConfigurationItem,caseTrace);
    }

    @Override
    public void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        super.attachConfiguredFactorsToCaseTrace(condition,serviceHistory,caseTrace);
    }


}