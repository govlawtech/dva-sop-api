package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.FixedDaysPeriodSelector;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.time.Period;
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

    private class SpecialNavyOfficerRuleApplicationResult {
        private final boolean ruleApplies;
        private final boolean onsetAfterSeparation;
        private final LocalDate onsetDate;
        private final Optional<LocalDate> separationDate;
        private final Optional<Service> relevantService;

        public SpecialNavyOfficerRuleApplicationResult(boolean ruleApplies, boolean onsetAfterSeparation, LocalDate onsetDate, Optional<LocalDate> separationDate, Optional<Service> relevantService)
        {

            this.ruleApplies = ruleApplies;
            this.onsetAfterSeparation = onsetAfterSeparation;
            this.onsetDate = onsetDate;
            this.separationDate = separationDate;
            this.relevantService = relevantService;
        }


        public boolean isRuleApplies() {
            return ruleApplies;
        }

        public boolean isOnsetAfterSeparation() {
            return onsetAfterSeparation;
        }

        public LocalDate getOnsetDate() {
            return onsetDate;
        }

        public Optional<LocalDate> getSeparationDate() {
            return separationDate;
        }

        public Optional<Service> getRelevantService() {
            return relevantService;
        }
    }


    private  SpecialNavyOfficerRuleApplicationResult SpecialNavyOfficerRuleApplies(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        Optional<Service> serviceProximateToCondition = ProcessingRuleFunctions.identifyCFTSServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(), condition.getStartDate(), caseTrace);
        if (!serviceProximateToCondition.isPresent())
        {
            return new SpecialNavyOfficerRuleApplicationResult(false,false,condition.getStartDate(),Optional.empty(),Optional.empty());
        }
        else
            {
            Rank rank = serviceProximateToCondition.get().getRank();
            ServiceBranch serviceBranch = serviceProximateToCondition.get().getBranch();
            if (rank == Rank.Officer && serviceBranch == ServiceBranch.RAN)
            {
                Optional<LocalDate> separationDate = serviceProximateToCondition.get().getEndDate();
                if (separationDate.isPresent() && separationDate.get().isBefore(condition.getStartDate()))
                {
                    Integer daysBetweenSeparationAndOnset = Period.between(separationDate.get(),condition.getStartDate()).getDays();
                    if (daysBetweenSeparationAndOnset <= 30)
                    {
                        return new SpecialNavyOfficerRuleApplicationResult(true,true,condition.getStartDate(),separationDate,serviceProximateToCondition);
                    }
                    else {
                        return new SpecialNavyOfficerRuleApplicationResult(false,true,condition.getStartDate(),separationDate,serviceProximateToCondition);
                    }
                }
                else {
                    return new SpecialNavyOfficerRuleApplicationResult(true,false,condition.getStartDate(),separationDate,serviceProximateToCondition);
                }
            }
            else {
                return new SpecialNavyOfficerRuleApplicationResult(false,false,condition.getStartDate(),Optional.empty(),serviceProximateToCondition);
            }
        }
    }

//    private static ApplicableWearAndTearRuleConfiguration BuildSpecialConfigurationForNavyOfficers() {
//        return new ApplicableWearAndTearRuleConfiguration() {
//            @Override
//            public String getConditionName() {
//                return null;
//            }
//
//            @Override
//            public RHRuleConfigurationItem getRHRuleConfigurationItem() {
//                return null;
//            }
//
//            @Override
//            public Optional<BoPRuleConfigurationItem> getBopRuleConfigurationItem() {
//                return Optional.empty();
//            }
//        }
//    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory,  CaseTrace caseTrace) {
        if (rhIntervalUsed == null)
        {
            throw new IllegalStateException("Must determine applicable SoP before getting satisfied factors");
        }

        // special rule for Navy Officers with onset during service, or within 30 days of separation from permanent forces
//        SpecialNavyOfficerRuleApplicationResult specialNavyOfficerRuleApplicationResult = SpecialNavyOfficerRuleApplies(condition,serviceHistory,caseTrace);
//        if (specialNavyOfficerRuleApplicationResult.ruleApplies)
//        {
//            ImmutableList<Factor> applicableFactors = condition.getApplicableFactors(applicableSop);
//
//            Optional<? extends RuleConfigurationItem> applicableRuleConfigurationItemOpt
//                    = applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis ?
//                    Optional.of(applicableWearAndTearRuleConfiguration.getRHRuleConfigurationItem())
//                    : applicableWearAndTearRuleConfiguration.getBopRuleConfigurationItem();
//
//            if (!applicableRuleConfigurationItemOpt.isPresent()) {
//                caseTrace.addLoggingTrace("No applicable rule configuration present for this combination of rank and service branch for this standard of proof.  Therefore no factors are satisfied.");
//                return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, ImmutableSet.of());
//            }
//
//            ImmutableList<FactorWithSatisfaction> inferredFactors =
//                    ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, applicableRuleConfigurationItemOpt.get().getMainFactorReferences());
//
//            return inferredFactors;
//        }

        Interval testInterval = applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis ?
                rhIntervalUsed : new FixedDaysPeriodSelector(240).getInterval(serviceHistory,condition.getStartDate());


        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory,testInterval, applicableWearAndTearRuleConfiguration,caseTrace);
    }

    @Override
    public ApplicableWearAndTearRuleConfiguration getApplicableWearAndTearRuleConfiguration() {
        return applicableWearAndTearRuleConfiguration;
    }

}
