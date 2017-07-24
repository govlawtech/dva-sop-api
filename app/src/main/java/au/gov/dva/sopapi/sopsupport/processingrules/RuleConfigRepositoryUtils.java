package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.*;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuleConfigRepositoryUtils {
    public static Optional<RHRuleConfigurationItem> getRelevantRHConfiguration(String conditionName, Rank rank, ServiceBranch serviceBranch, RuleConfigurationRepository ruleConfigurationRepository)
    {
        Optional<RHRuleConfigurationItem> item = ruleConfigurationRepository.getRHItems().stream()
                .filter(i -> i.getConditionName().equalsIgnoreCase(conditionName))
                .filter(i -> i.getRank() == rank)
                .filter(i -> i.getServiceBranch() == serviceBranch)
                .findFirst();
        return item;
    }

    public static Optional<BoPRuleConfigurationItem> getRelevantBoPConfiguration(String conditionName, Rank rank, ServiceBranch serviceBranch, RuleConfigurationRepository ruleConfigurationRepository) {
        Optional<BoPRuleConfigurationItem> item = ruleConfigurationRepository.getBoPItems().stream()
                .filter(i -> i.getConditionName().equalsIgnoreCase(conditionName))
                .filter(i -> i.getRank() == rank)
                .filter(i -> i.getServiceBranch() == serviceBranch)
                .findFirst();
        return item;
    }

    public static Optional<RuleConfigurationItem> getApplicableRuleConfigurationItem(StandardOfProof standardOfProof, String conditionName, Rank rank, ServiceBranch serviceBranch, RuleConfigurationRepository ruleConfigurationRepository)
    {
        if (standardOfProof == StandardOfProof.ReasonableHypothesis)
        {
             Optional<RHRuleConfigurationItem> r = getRelevantRHConfiguration(conditionName,rank,serviceBranch,ruleConfigurationRepository);
             return r.isPresent() ? Optional.of(r.get()) : Optional.empty();

        }
        else {
            Optional<BoPRuleConfigurationItem> r = getRelevantBoPConfiguration(conditionName,rank,serviceBranch,ruleConfigurationRepository);
            return r.isPresent() ? Optional.of(r.get()) : Optional.empty();
        }
    }


    public static Optional<Integer> getOperationalServiceTestPeriodForCondition(RuleConfigurationRepository ruleConfigurationRepository, String conditionName)
    {
        Optional<RHRuleConfigurationItem> item = ruleConfigurationRepository.getRHItems().stream()
                .filter(i -> i.getConditionName().equalsIgnoreCase(conditionName))
                .filter(i -> i.getYearsLimitForOperationalService().isPresent())
                .findFirst();

        if (item.isPresent())
        {
            return Optional.of(item.get().getYearsLimitForOperationalService().get());
        }
        else {
            return Optional.empty();
        }

    }


    public static boolean containsConfigForCondition(String conditionName, RuleConfigurationRepository ruleConfigurationRepository)
    {
        return ruleConfigurationRepository.getRHItems()
                .stream()
                .anyMatch(i -> i.getConditionName().equalsIgnoreCase(conditionName));
    }



    public ImmutableSet<ConditionConfiguration> getConditionConfigurations(RuleConfigurationRepository ruleConfigurationRepository)
    {
        Set<String> rhConditionNames = ruleConfigurationRepository.getRHItems().stream().map(ruleConfigurationItem -> ruleConfigurationItem.getConditionName().toLowerCase().trim()).collect(Collectors.toSet());
        Set<String> boPConditionNames = ruleConfigurationRepository.getBoPItems().stream().map(boPRuleConfigurationItem -> boPRuleConfigurationItem.getConditionName().toLowerCase().trim()).collect(Collectors.toSet());
        ImmutableSet<String> conditionNames = Sets.union(rhConditionNames,boPConditionNames).immutableCopy();

        Map<String,List<RHRuleConfigurationItem>> rhRuleConfigurationItemsGroupedByConditionName = ruleConfigurationRepository.getRHItems()
                .stream()
                .collect(Collectors.groupingBy(o -> o.getConditionName()));

        Map<String,List<BoPRuleConfigurationItem>> boPRuleConfigurationItemsGroupedByConditionName = ruleConfigurationRepository.getBoPItems()
                .stream()
                .collect(Collectors.groupingBy(o -> o.getConditionName()));

        List<ConditionConfiguration> conditionConfigurations = conditionNames.stream().map(s -> {

            ImmutableSet<RHRuleConfigurationItem> rhRuleConfigurationItems = rhRuleConfigurationItemsGroupedByConditionName.containsKey(s) ? ImmutableSet.copyOf(rhRuleConfigurationItemsGroupedByConditionName.get(s)) : ImmutableSet.of();
            ImmutableSet<BoPRuleConfigurationItem> boPRuleConfigurationItems = boPRuleConfigurationItemsGroupedByConditionName.containsKey(s) ? ImmutableSet.copyOf(boPRuleConfigurationItemsGroupedByConditionName.get(s)) : ImmutableSet.of();
            return new ConditionConfigurationImpl(s,rhRuleConfigurationItems,boPRuleConfigurationItems);

        }).collect(Collectors.toList());

        return ImmutableSet.copyOf(conditionConfigurations);
    }

}
