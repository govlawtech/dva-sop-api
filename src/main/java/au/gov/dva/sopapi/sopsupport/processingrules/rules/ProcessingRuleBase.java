package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Predicate;

import static au.gov.dva.sopapi.sopsupport.processingrules.RuleConfigRepositoryUtils.getApplicableRuleConfigurationItem;
import static au.gov.dva.sopapi.sopsupport.processingrules.RuleConfigRepositoryUtils.getRelevantRHConfiguration;

public class ProcessingRuleBase {

    protected Optional<SoP> getApplicableSop(RuleConfigurationRepository ruleConfigurationRepository, Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        Optional<Rank> relevantRank = ProcessingRuleFunctions.getRankProximateToDate(serviceHistory.getServices(),condition.getStartDate(),caseTrace);
        if (!relevantRank.isPresent())
        {
            caseTrace.addTrace("Cannot determine the relevant rank, therefore cannot apply STP rules to determine the applicable SoP.");
            return Optional.empty();
        }

        Optional<Service> serviceDuringWhichConditionStarts =  ProcessingRuleFunctions.identifyServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(),condition.getStartDate(), caseTrace);
        if (!serviceDuringWhichConditionStarts.isPresent())
        {
            caseTrace.addTrace("Cannot find any Service during or after which the condition started, therefore there is no applicable SoP.");
            return Optional.empty();
        }

        Optional<RHRuleConfigurationItem> rhRuleConfigurationItemOptional = getRelevantRHConfiguration(
                condition.getSopPair().getConditionName(),
                relevantRank.get(),
                serviceDuringWhichConditionStarts.get().getBranch(),
                ruleConfigurationRepository);
        if (!rhRuleConfigurationItemOptional.isPresent())
        {
            caseTrace.addTrace(String.format("Cannot find any rule for Reasonable Hypothesis for the condition of %s, for the rank of %s, for the service branch of %s.  Therefore, cannot determine whether BoP or RH SoP applies.",
                    condition.getSopPair().getConditionName(),
                    relevantRank.get(),
                    serviceDuringWhichConditionStarts.get().getBranch()));
            return Optional.empty();
        }
        RHRuleConfigurationItem rhRuleConfigurationItem = rhRuleConfigurationItemOptional.get();

        OffsetDateTime startDateForPeriodOfOperationalService = condition.getStartDate().minusYears(rhRuleConfigurationItem.getYearsLimitForOperationalService());
        caseTrace.addTrace(String.format("The start date for the test period of operational service is %s years before the condition start date of %s: %s.",
                rhRuleConfigurationItem.getRequiredDaysOfOperationalService(),
                condition.getStartDate(),
                startDateForPeriodOfOperationalService));
        OffsetDateTime endDateForPeriodOfOperationalService = condition.getStartDate();
        caseTrace.addTrace("The end date for the test period of operational service is the condition start date: " + condition.getStartDate());
        long daysOfOperationalService = ProcessingRuleFunctions.getNumberOfDaysOfOperationalServiceInInterval(
                startDateForPeriodOfOperationalService,endDateForPeriodOfOperationalService,
                ProcessingRuleFunctions.getDeployments(serviceHistory),
                isOperational, caseTrace);

        Integer minimumRequiredDaysOfOperationalServiceForRank = rhRuleConfigurationItem.getRequiredDaysOfOperationalService();
        caseTrace.addTrace("Required number of days of operational service: " + minimumRequiredDaysOfOperationalServiceForRank);

        if (minimumRequiredDaysOfOperationalServiceForRank.longValue() <= daysOfOperationalService)
        {
            caseTrace.addTrace("The RH SoP is applicable as the actual number of days of operational service in the test period is greater than or equal to the required number.");
            return Optional.of(condition.getSopPair().getRhSop());
        }
        else {

            caseTrace.addTrace("The BoP SoP is applicable as the actual number of days of operational service in the test period is less than the required number.");
            return Optional.of(condition.getSopPair().getBopSop());
        }
    }

    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(RuleConfigurationRepository ruleConfigurationRepository, Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {

        ImmutableList<Factor> applicableFactors =  condition.getApplicableFactors(applicableSop);
        caseTrace.addTrace(String.format("There are %s factors in the applicable SoP: %s", applicableFactors.size(),applicableSop.getCitation()));

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
            caseTrace.addTrace(String.format("No rule configured for condition of %s, for standard of proof of %s, for rank of %s, for service branch of %s.  Therefore, no satisfied factors.",
                    condition.getSopPair().getConditionName(),
                    applicableSop.getStandardOfProof(),
                    relevantRank.get(),
                    serviceDuringWhichConditionStarts.get().getBranch()
            ));
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors,ImmutableSet.of());
        }

        RuleConfigurationItem applicableRuleConfig = applicableRuleConfigItemOpt.get();

        Integer cftsDaysRequired = applicableRuleConfig.getRequiredCFTSWeeks() * 7;
        caseTrace.addTrace("Required days of continuous full time service: " + cftsDaysRequired);
        Long actualDaysOfCfts = ProcessingRuleFunctions.getDaysOfContinuousFullTimeServiceToDate(serviceHistory,condition.getStartDate());
        caseTrace.addTrace("Actual days of continuous full time service:" + actualDaysOfCfts);

        if (actualDaysOfCfts >= cftsDaysRequired) {
            caseTrace.addTrace("Actual number of days of continuous full time service is at least the required days.  Therefore, returning satisfied factors according to configuration.");
            ImmutableList<FactorWithSatisfaction> inferredFactors =
                    ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, applicableRuleConfig.getFactorReferences());

            return inferredFactors;
        }
        else
        {
            caseTrace.addTrace("Actual number of days of continuous full time service is less that the required days.  Therefore, no factors are satisfied.");
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, ImmutableSet.of());
        }
    }

}
