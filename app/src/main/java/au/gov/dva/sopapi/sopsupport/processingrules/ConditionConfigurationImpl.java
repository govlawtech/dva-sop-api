package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.ConditionConfiguration;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;
import com.google.common.collect.ImmutableSet;

public class ConditionConfigurationImpl implements ConditionConfiguration
{

    private final String conditionName;
    private final ImmutableSet<RHRuleConfigurationItem> rhItems;
    private final ImmutableSet<BoPRuleConfigurationItem> bopItems;

    public ConditionConfigurationImpl(String conditionName, ImmutableSet<RHRuleConfigurationItem> rhItems, ImmutableSet<BoPRuleConfigurationItem> bopItems ) {

        this.conditionName = conditionName;
        this.rhItems = rhItems;
        this.bopItems = bopItems;
    }

    @Override
    public String getConditionName() {
        return conditionName;
    }

    @Override
    public ImmutableSet<RHRuleConfigurationItem> getRHRuleConfigurationItems() {
        return rhItems;
    }

    @Override
    public ImmutableSet<BoPRuleConfigurationItem> getBoPRuleConfigurationItems() {
        return bopItems;
    }
}
