package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.exceptions.ProcessingRuleError;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import au.gov.dva.sopapi.sopsupport.processingrules.RuleConfigRepositoryUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static au.gov.dva.sopapi.sopsupport.processingrules.RuleConfigRepositoryUtils.getApplicableRuleConfigurationItem;
import static au.gov.dva.sopapi.sopsupport.processingrules.RuleConfigRepositoryUtils.getRelevantRHConfiguration;

public class GenericProcessingRule implements ProcessingRule {

    protected RuleConfigurationRepository ruleConfigurationRepository;

    public GenericProcessingRule(RuleConfigurationRepository ruleConfigurationRepository)
    {
        this.ruleConfigurationRepository = ruleConfigurationRepository;
    }

    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        Optional<Rank> relevantRank = ProcessingRuleFunctions.getRankProximateToDate(serviceHistory.getServices(),condition.getStartDate(),caseTrace);
        if (!relevantRank.isPresent())
        {
            caseTrace.addLoggingTrace("Cannot determine the relevant rank, therefore cannot apply STP rules to determine the applicable SoP.");
            return Optional.empty();
        }

        Optional<Service> serviceDuringWhichConditionStarts =  ProcessingRuleFunctions.identifyServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(),condition.getStartDate(), caseTrace);
        if (!serviceDuringWhichConditionStarts.isPresent())
        {
            caseTrace.addLoggingTrace("Cannot find any Service during or after which the condition started, therefore there is no applicable SoP.");
            return Optional.empty();
        }

        Optional<RHRuleConfigurationItem> rhRuleConfigurationItemOptional = getRelevantRHConfiguration(
                condition.getSopPair().getConditionName(),
                relevantRank.get(),
                serviceDuringWhichConditionStarts.get().getBranch(),
                ruleConfigurationRepository);

        if (!rhRuleConfigurationItemOptional.isPresent())
        {
            caseTrace.addLoggingTrace(String.format("Cannot find any rule for Reasonable Hypothesis for the condition of %s, for the rank of %s, for the service branch of %s.  Therefore, cannot determine whether BoP or RH SoP applies.",
                    condition.getSopPair().getConditionName(),
                    relevantRank.get(),
                    serviceDuringWhichConditionStarts.get().getBranch()));
            return Optional.empty();
        }

        RHRuleConfigurationItem rhRuleConfigurationItem = rhRuleConfigurationItemOptional.get();

        OffsetDateTime startDateForPeriodOfOperationalService = condition.getStartDate().minusYears(rhRuleConfigurationItem.getYearsLimitForOperationalService());
        caseTrace.addLoggingTrace(String.format("The start date for the test period of operational service is %s years before the condition start date of %s: %s.",
                rhRuleConfigurationItem.getRequiredDaysOfOperationalService(),
                condition.getStartDate(),
                startDateForPeriodOfOperationalService));
        OffsetDateTime endDateForPeriodOfOperationalService = condition.getStartDate();
        caseTrace.addLoggingTrace("The end date for the test period of operational service is the condition start date: " + condition.getStartDate());
        Long daysOfOperationalService = ProcessingRuleFunctions.getNumberOfDaysOfOperationalServiceInInterval(
                startDateForPeriodOfOperationalService,endDateForPeriodOfOperationalService,
                ProcessingRuleFunctions.getDeployments(serviceHistory),
                isOperational, caseTrace);

        if (daysOfOperationalService >= Integer.MAX_VALUE)
        {
            throw new ProcessingRuleError("Cannot handle days of operational service more than " + Integer.MAX_VALUE);  // for the appeasement of find bugs
        }
        caseTrace.setActualOperationalDays(daysOfOperationalService.intValue());

        Integer minimumRequiredDaysOfOperationalServiceForRank = rhRuleConfigurationItem.getRequiredDaysOfOperationalService();
        caseTrace.addLoggingTrace("Required number of days of operational service: " + minimumRequiredDaysOfOperationalServiceForRank);
        caseTrace.setRequiredOperationalDaysForRh(minimumRequiredDaysOfOperationalServiceForRank);

        if (minimumRequiredDaysOfOperationalServiceForRank.longValue() <= daysOfOperationalService)
        {
            caseTrace.addLoggingTrace("The RH SoP is applicable as the actual number of days of operational service in the test period is greater than or equal to the required number.");
            caseTrace.setApplicableStandardOfProof(StandardOfProof.ReasonableHypothesis);
            return Optional.of(condition.getSopPair().getRhSop());
        }
        else {
            caseTrace.addLoggingTrace("The BoP SoP is applicable as the actual number of days of operational service in the test period is less than the required number.");
            caseTrace.setApplicableStandardOfProof(StandardOfProof.BalanceOfProbabilities);
            return Optional.of(condition.getSopPair().getBopSop());
        }
    }

    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {

        ImmutableList<Factor> applicableFactors =  condition.getApplicableFactors(applicableSop);
        caseTrace.addLoggingTrace(String.format("There are %s factors in the applicable SoP: %s", applicableFactors.size(),applicableSop.getCitation()));

        Optional<Rank> relevantRank = ProcessingRuleFunctions.getRankProximateToDate(serviceHistory.getServices(),condition.getStartDate(),caseTrace);
        Optional<Service> serviceDuringWhichConditionStarts =  ProcessingRuleFunctions.identifyServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(),condition.getStartDate(),caseTrace);
        if (!relevantRank.isPresent() || !serviceDuringWhichConditionStarts.isPresent())
        {
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, ImmutableSet.of());
        }

        Optional<RuleConfigurationItem> applicableRuleConfigItemOpt = getApplicableRuleConfigurationItem(applicableSop.getStandardOfProof(),condition.getSopPair().getConditionName(),
                relevantRank.get(),
                serviceDuringWhichConditionStarts.get().getBranch(),
                ruleConfigurationRepository);

        if (!applicableRuleConfigItemOpt.isPresent())
        {
            caseTrace.addLoggingTrace(String.format("No rule configured for condition of %s, for standard of proof of %s, for rank of %s, for service branch of %s.  Therefore, no satisfied factors.",
                    condition.getSopPair().getConditionName(),
                    applicableSop.getStandardOfProof(),
                    relevantRank.get(),
                    serviceDuringWhichConditionStarts.get().getBranch()
            ));
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors,ImmutableSet.of());
        }

        RuleConfigurationItem applicableRuleConfig = applicableRuleConfigItemOpt.get();

        Integer cftsDaysRequired = applicableRuleConfig.getRequiredCFTSWeeks() * 7;
        caseTrace.setRequiredCftsDays(cftsDaysRequired);

        caseTrace.addLoggingTrace("Required days of continuous full time service: " + cftsDaysRequired);
        Long actualDaysOfCfts = ProcessingRuleFunctions.getDaysOfContinuousFullTimeServiceToDate(serviceHistory,condition.getStartDate());
        if (actualDaysOfCfts >= Integer.MAX_VALUE)
        {
            throw new ProcessingRuleError("Cannot handle days of CFTS service more than " + Integer.MAX_VALUE);  // for the appeasement of find bugs
        }
        caseTrace.setActualCftsDays(actualDaysOfCfts.intValue());
        caseTrace.addLoggingTrace("Actual days of continuous full time service:" + actualDaysOfCfts);

        if (actualDaysOfCfts >= cftsDaysRequired) {
            caseTrace.addLoggingTrace("Actual number of days of continuous full time service is at least the required days.  Therefore, returning satisfied factors according to configuration.");
            ImmutableList<FactorWithSatisfaction> inferredFactors =
                    ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, applicableRuleConfig.getFactorReferences());

            return inferredFactors;
        }
        else
        {
            caseTrace.addLoggingTrace("Actual number of days of continuous full time service is less that the required days.  Therefore, no factors are satisfied.");
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, ImmutableSet.of());
        }
    }

    public void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        Optional<Rank> relevantRank = ProcessingRuleFunctions.getRankProximateToDate(serviceHistory.getServices(),condition.getStartDate(),caseTrace);
        Optional<Service> serviceDuringWhichConditionStarts =  ProcessingRuleFunctions.identifyServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(),condition.getStartDate(),caseTrace);
        if (relevantRank.isPresent() && serviceDuringWhichConditionStarts.isPresent())
        {
            // BoP
            ImmutableList<Factor> bopFactors = condition.getApplicableFactors(condition.getSopPair().getBopSop());
            Optional<BoPRuleConfigurationItem> BoPRuleConfigItemOpt = RuleConfigRepositoryUtils.getRelevantBoPConfiguration(condition.getSopPair().getConditionName(),
                    relevantRank.get(),
                    serviceDuringWhichConditionStarts.get().getBranch(),
                    ruleConfigurationRepository);
            if (BoPRuleConfigItemOpt.isPresent()){
                ImmutableSet<String> bopFactorParagraphs = BoPRuleConfigItemOpt.get().getFactorReferences();
                List<Factor> applicableBopFactors = bopFactors.stream().filter(f -> bopFactorParagraphs.contains(f.getParagraph())).collect(Collectors.toList());
                caseTrace.setBopFactors(ImmutableList.copyOf(applicableBopFactors));
            }

            // RH
            ImmutableList<Factor> rhFactors = condition.getApplicableFactors(condition.getSopPair().getRhSop());
            Optional<RHRuleConfigurationItem> RHRuleConfigItemOpt = RuleConfigRepositoryUtils.getRelevantRHConfiguration(condition.getSopPair().getConditionName(),
                    relevantRank.get(),
                    serviceDuringWhichConditionStarts.get().getBranch(),
                    ruleConfigurationRepository);
            if (RHRuleConfigItemOpt.isPresent()){
                ImmutableSet<String> rhFactorParagraphs = RHRuleConfigItemOpt.get().getFactorReferences();
                List<Factor> applicableRhFactors = rhFactors.stream().filter(f -> rhFactorParagraphs.contains(f.getParagraph())).collect(Collectors.toList());
                caseTrace.setRhFactors(ImmutableList.copyOf(applicableRhFactors));
            }
        }


    }

}
