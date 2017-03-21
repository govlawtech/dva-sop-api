package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.Rank;
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

    protected Optional<SoP> getApplicableSop(RuleConfigurationRepository ruleConfigurationRepository, Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational) {

        Optional<Rank> relevantRank = ProcessingRuleFunctions.getRankProximateToDate(serviceHistory.getServices(),condition.getStartDate());
        Optional<Service> serviceDuringWhichConditionStarts =  ProcessingRuleFunctions.identifyServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(),condition.getStartDate());
        if (!relevantRank.isPresent() || !serviceDuringWhichConditionStarts.isPresent())
        {
            return Optional.empty();
        }

        Optional<RHRuleConfigurationItem> rhRuleConfigurationItemOptional = getRelevantRHConfiguration(
                condition.getSopPair().getConditionName(),
                relevantRank.get(),
                serviceDuringWhichConditionStarts.get().getBranch(),
                ruleConfigurationRepository);
        if (!rhRuleConfigurationItemOptional.isPresent())
        {
            return Optional.empty();
        }
        RHRuleConfigurationItem rhRuleConfigurationItem = rhRuleConfigurationItemOptional.get();

        OffsetDateTime startDateForPeriodOfOperationalService = condition.getStartDate().minusYears(rhRuleConfigurationItem.getYearsLimitForOperationalService());
        OffsetDateTime endDateForPeriodOfOperationalService = condition.getStartDate();
        long daysOfOperationalService = ProcessingRuleFunctions.getNumberOfDaysOfOperationalServiceInInterval(
                startDateForPeriodOfOperationalService,endDateForPeriodOfOperationalService,
                ProcessingRuleFunctions.getDeployments(serviceHistory),
                isOperational);

        Integer minimumRequiredDaysOfOperationalServiceForRank = rhRuleConfigurationItem.getRequiredDaysOfOperationalService();

        if (minimumRequiredDaysOfOperationalServiceForRank.longValue() <= daysOfOperationalService)
        {
            return Optional.of(condition.getSopPair().getRhSop());
        }
        else return Optional.of(condition.getSopPair().getBopSop());
    }

    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(RuleConfigurationRepository ruleConfigurationRepository, Condition condition, SoP applicableSop, ServiceHistory serviceHistory) {

        ImmutableList<Factor> applicableFactors =  condition.getApplicableFactors(applicableSop);
        Optional<Rank> relevantRank = ProcessingRuleFunctions.getRankProximateToDate(serviceHistory.getServices(),condition.getStartDate());
        Optional<Service> serviceDuringWhichConditionStarts =  ProcessingRuleFunctions.identifyServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(),condition.getStartDate());
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
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors,ImmutableSet.of());
        }

        RuleConfigurationItem applicableRuleConfig = applicableRuleConfigItemOpt.get();

        Integer cftsDaysRequired = applicableRuleConfig.getRequiredCFTSWeeks() * 7;
        Long actualDaysOfCfts = ProcessingRuleFunctions.getDaysOfContinuousFullTimeServiceToDate(serviceHistory,condition.getStartDate());

        if (actualDaysOfCfts >= cftsDaysRequired) {
            ImmutableList<FactorWithSatisfaction> inferredFactors =
                    ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, applicableRuleConfig.getFactorReferences());

            return inferredFactors;
        }
        else
        {
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, ImmutableSet.of());
        }
    }

}
