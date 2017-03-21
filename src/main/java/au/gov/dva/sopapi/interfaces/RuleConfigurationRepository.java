package au.gov.dva.sopapi.interfaces;

import com.google.common.collect.ImmutableSet;

public interface RuleConfigurationRepository {
    ImmutableSet<RHRuleConfigurationItem> getRHItems();
    ImmutableSet<BoPRuleConfigurationItem> getBoPItems();
}


