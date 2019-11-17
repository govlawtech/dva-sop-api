package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.sopsupport.processingrules.ConditionConfigurationImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.RuleConfigRepositoryUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface RuleConfigurationRepository {
    ImmutableSet<RHRuleConfigurationItem> getRHItems();
    ImmutableSet<BoPRuleConfigurationItem> getBoPItems();

    default boolean containsRHConfigForCondition(String conditionName)
    {
        return this.getRHItems()
                .stream()
                .anyMatch(i -> i.getConditionName().equalsIgnoreCase(conditionName));
    }

    default Optional<ConditionConfiguration> getConditionConfigurationFor(String conditionName)
    {
        Optional<ConditionConfiguration> cc = getConditionConfigurations()
                .stream()
                .filter(conditionConfiguration -> conditionConfiguration.getConditionName().equalsIgnoreCase(conditionName))
                .findFirst();
        return cc;
    }

    default ImmutableSet<ConditionConfiguration> getConditionConfigurations()
    {
        Set<String> rhConditionNames = this.getRHItems().stream().map(ruleConfigurationItem -> ruleConfigurationItem.getConditionName().toLowerCase().trim()).collect(Collectors.toSet());
        Set<String> boPConditionNames = this.getBoPItems().stream().map(boPRuleConfigurationItem -> boPRuleConfigurationItem.getConditionName().toLowerCase().trim()).collect(Collectors.toSet());
        ImmutableSet<String> conditionNames = Sets.union(rhConditionNames,boPConditionNames).immutableCopy();

        Map<String,List<RHRuleConfigurationItem>> rhRuleConfigurationItemsGroupedByConditionName = this.getRHItems()
                .stream()
                .collect(Collectors.groupingBy(o -> o.getConditionName()));

        Map<String,List<BoPRuleConfigurationItem>> boPRuleConfigurationItemsGroupedByConditionName = this.getBoPItems()
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
