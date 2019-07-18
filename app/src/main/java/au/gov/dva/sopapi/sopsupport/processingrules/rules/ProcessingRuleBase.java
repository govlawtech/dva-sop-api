package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.exceptions.ProcessingRuleRuntimeException;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.ApplicableRuleConfigurationImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class ProcessingRuleBase {
    protected final ApplicableWearAndTearRuleConfiguration applicableWearAndTearRuleConfiguration;

    public ProcessingRuleBase(ApplicableWearAndTearRuleConfiguration applicableWearAndTearRuleConfiguration) {
        this.applicableWearAndTearRuleConfiguration = applicableWearAndTearRuleConfiguration;
    }


    protected boolean shouldAbortProcessing(ServiceHistory serviceHistory, Condition condition, CaseTrace caseTrace) {

        Optional<Rank> relevantRank = ProcessingRuleFunctions.getCFTSRankProximateToDate(serviceHistory.getServices(), condition.getStartDate(), caseTrace);
        if (!relevantRank.isPresent()) {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, "Cannot determine the relevant rank, therefore cannot apply STP rules to determine the applicable SoP.");
            return true;
        }

        if (!RulePreconditions.isServiceHistoryInternallyConsistent(serviceHistory,caseTrace)) return true;

        if (!RulePreconditions.serviceExistsBeforeConditionOnset(serviceHistory,condition,caseTrace)) return true;

        return false;
    }

    public static ImmutableList<Factor> getApplicableFactors(SoP sop, ImmutableSet<String> configuredFactors)
    {
        return sop.getOnsetFactors().stream()
        .filter(f -> configuredFactors.contains(f.getParagraph()))
                .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));
    }


    protected Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, Interval rhInterval, boolean defaultToNoneIfBoP, CaseTrace caseTrace) {

        Optional<Service> serviceDuringOrAfterWhichConditionStarts = ProcessingRuleFunctions.identifyCFTSServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(), condition.getStartDate(), caseTrace);

        if (!serviceDuringOrAfterWhichConditionStarts.isPresent())
        {
            caseTrace.addLoggingTrace("The service history does not show any continuous full time service before or during the condition onset.");
            return Optional.empty();
        }

        Integer requiredOperationalServiceDaysToApplyRhSop = applicableWearAndTearRuleConfiguration.getRHRuleConfigurationItem().getRequiredDaysOfOperationalService();

        Interval testInterval = rhInterval;
        caseTrace.addLoggingTrace(String.format("The start date for the test period for RH: %s", testInterval.getStart()));
        caseTrace.addLoggingTrace(String.format("The end date for the test period for RH: %s", condition.getStartDate()));

        Long daysOfOperationalService = serviceHistory.getNumberOfDaysOfFullTimeOperationalService(testInterval.getStart(),testInterval.getEnd(),isOperational);

        if (daysOfOperationalService >= Integer.MAX_VALUE) {
            throw new ProcessingRuleRuntimeException("Cannot handle days of operational service more than " + Integer.MAX_VALUE);  // for the appeasement of find bugs
        }
        caseTrace.setActualOperationalDays(daysOfOperationalService.intValue());

        // applicable factors
        ImmutableList<Factor> applicableRhFactors = getApplicableFactors(condition.getSopPair().getRhSop(),applicableWearAndTearRuleConfiguration.getRHRuleConfigurationItem().getFactorReferences());
        ImmutableList<Factor> applicableBoPFactors = applicableWearAndTearRuleConfiguration.getBopRuleConfigurationItem().isPresent() ? getApplicableFactors(condition.getSopPair().getBopSop(), applicableWearAndTearRuleConfiguration.getBopRuleConfigurationItem().get().getFactorReferences()) : ImmutableList.of();

        caseTrace.setRhFactors(applicableRhFactors);
        caseTrace.setBopFactors(applicableBoPFactors);

        caseTrace.addReasoningFor(ReasoningFor.STANDARD_OF_PROOF, String.format("Required number of days of operational service for Reasonable Hypothesis: %d.", requiredOperationalServiceDaysToApplyRhSop));
        caseTrace.setRequiredOperationalDaysForRh(requiredOperationalServiceDaysToApplyRhSop);
        caseTrace.addReasoningFor(ReasoningFor.STANDARD_OF_PROOF, String.format("Actual number of days of operational service: %d.", daysOfOperationalService));

        if (requiredOperationalServiceDaysToApplyRhSop.longValue() <= daysOfOperationalService) {
            caseTrace.addLoggingTrace("The RH SoP is applicable as the actual number of days of operational service in the test period is greater than or equal to the required number.");
            caseTrace.setApplicableStandardOfProof(StandardOfProof.ReasonableHypothesis);

            caseTrace.setRhFactors(condition.getSopPair().getRhSop().getOnsetFactors());
            return Optional.of(condition.getSopPair().getRhSop());
        } else if (!defaultToNoneIfBoP) {
            caseTrace.addLoggingTrace("The BoP SoP is applicable as the actual number of days of operational service in the test period is less than the required number.");
            caseTrace.setApplicableStandardOfProof(StandardOfProof.BalanceOfProbabilities);
            caseTrace.setBopFactors(condition.getSopPair().getBopSop().getOnsetFactors());
            return Optional.of(condition.getSopPair().getBopSop());
        }
        else {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING,"Veteran Centric Processing only applies to the reasonable hypothesis standard for this condition.");
            return Optional.empty();
        }
    }

    protected Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, Interval rhInterval, CaseTrace caseTrace) {
        return getApplicableSop(condition, serviceHistory,  isOperational, rhInterval, false,  caseTrace);
    }


    protected ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, Interval testInterval, ApplicableWearAndTearRuleConfiguration applicableWearAndTearRuleConfiguration, CaseTrace caseTrace) {

        assert applicableSop.getConditionName().equalsIgnoreCase(condition.getSopPair().getConditionName());

        ImmutableList<Factor> applicableFactors = condition.getApplicableFactors(applicableSop);
        caseTrace.addLoggingTrace(String.format("There are %s factors in the applicable SoP: %s.", applicableFactors.size(), applicableSop.getCitation()));


        Optional<? extends RuleConfigurationItem> applicableRuleConfigurationItemOpt
                = applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis ?
                Optional.of(applicableWearAndTearRuleConfiguration.getRHRuleConfigurationItem())
                : applicableWearAndTearRuleConfiguration.getBopRuleConfigurationItem();

        if (!applicableRuleConfigurationItemOpt.isPresent()) {
            caseTrace.addLoggingTrace("No applicable rule configuration present for this combination of rank and service branch for this standard of proof.  Therefore no factors are satisfied.");
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, ImmutableSet.of());
        }

        RuleConfigurationItem applicableRuleConfigurationItem = applicableRuleConfigurationItemOpt.get();

        Integer cftsDaysRequired = applicableRuleConfigurationItem.getRequiredCFTSDays();
        caseTrace.setRequiredCftsDays(cftsDaysRequired);

        caseTrace.addReasoningFor(ReasoningFor.MEETING_FACTORS, "Required days of continuous full time service: " + cftsDaysRequired);

        caseTrace.addLoggingTrace(String.format("The start date for the test period for counting days of CFTS: %s", testInterval.getStart()));
        caseTrace.addLoggingTrace(String.format("The end date for the test period for counting days of CFTS: %s", condition.getStartDate()));

        ImmutableList<Service> cftsServices = serviceHistory.getServices().stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        Long actualDaysOfCfts = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(testInterval.getStart(), testInterval.getEnd(), cftsServices);

        if (actualDaysOfCfts >= Integer.MAX_VALUE) {
            throw new ProcessingRuleRuntimeException("Cannot handle days of CFTS service more than " + Integer.MAX_VALUE);  // for the appeasement of find bugs
        }

        caseTrace.setActualCftsDays(actualDaysOfCfts.intValue());
        caseTrace.addReasoningFor(ReasoningFor.MEETING_FACTORS, "Actual days of continuous full time service: " + actualDaysOfCfts);

        if (actualDaysOfCfts >= cftsDaysRequired) {
            caseTrace.addLoggingTrace(String.format("Actual number of days of continuous full time service is at least the required days.  According to configuration, the applicable factors are '%s'.",
                    String.join(", ", applicableRuleConfigurationItem.getFactorReferences())));
            ImmutableList<FactorWithSatisfaction> inferredFactors =
                    ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, applicableRuleConfigurationItem.getFactorReferences());

            return inferredFactors;
        } else {
            caseTrace.addLoggingTrace("Actual number of days of continuous full time service is less that the required days.  Therefore, no factors are satisfied.");
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, ImmutableSet.of());
        }
    }

}
