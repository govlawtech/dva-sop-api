package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.parsing.traits.ParaReferenceSplitter;
import au.gov.dva.sopapi.sopref.parsing.traits.SubFactorParser;
import au.gov.dva.sopapi.sopref.parsing.traits.SubParasHandler;
import au.gov.dva.sopapi.sopsupport.processingrules.*;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.SlidingCFTSSelectorWithYearLimit;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.SlidingOperationalServicePeriodSelector;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

    public class LumbarSpondylosisRule extends ProcessingRuleBase implements WearAndTearProcessingRule {

    Interval rhIntervalUsed;

    public LumbarSpondylosisRule(ApplicableWearAndTearRuleConfiguration applicableWearAndTearRuleConfiguration) {
        super(applicableWearAndTearRuleConfiguration);
    }

    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        if (super.shouldAbortProcessing(serviceHistory,condition,caseTrace))
        {
            return Optional.empty();
        }

        IntervalSelector intervalSelector = new SlidingOperationalServicePeriodSelector(isOperational,10);
        caseTrace.addLoggingTrace("For this condition, service within 'any ten year period' before clinical onset is relevant.  The days of service are taken from the ten year interval before condition onset with the most operational service.  If multiple ten year intervals have the same amount of operational service, the most recent is used.");
        Interval intervalForTestingRh = intervalSelector.getInterval(serviceHistory,condition.getStartDate());
        rhIntervalUsed = intervalForTestingRh;
        return super.getApplicableSop(condition,serviceHistory,isOperational,intervalForTestingRh,caseTrace);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory,  CaseTrace caseTrace) {

        if (rhIntervalUsed == null)
        {
            throw new IllegalStateException("Must determine applicable SoP before getting satisfied factors");
        }
        Interval testInterval = applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis ?
                rhIntervalUsed : (new SlidingCFTSSelectorWithYearLimit( 10,25).getInterval(serviceHistory,condition.getStartDate()));

        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory,testInterval, applicableWearAndTearRuleConfiguration,caseTrace);
    }

    @Override
    public ApplicableWearAndTearRuleConfiguration getApplicableWearAndTearRuleConfiguration() {
        return applicableWearAndTearRuleConfiguration;
    }


}