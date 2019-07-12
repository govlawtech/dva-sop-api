package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.ApplicableWearAndTearRuleConfiguration;
import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;

import java.util.Optional;

public class ApplicableRuleConfigurationImpl implements ApplicableWearAndTearRuleConfiguration {

    private final RHRuleConfigurationItem rhRuleConfigurationItem;
    private final Optional<BoPRuleConfigurationItem> boPRuleConfigurationItem;

    public ApplicableRuleConfigurationImpl(RHRuleConfigurationItem rhRuleConfigurationItem, Optional<BoPRuleConfigurationItem> boPRuleConfigurationItem)
    {

        this.rhRuleConfigurationItem = rhRuleConfigurationItem;
        this.boPRuleConfigurationItem = boPRuleConfigurationItem;
    }

    @Override
    public RHRuleConfigurationItem getRHRuleConfigurationItem() {
        return rhRuleConfigurationItem;
    }

    @Override
    public Optional<BoPRuleConfigurationItem> getBopRuleConfigurationItem() {
        return boPRuleConfigurationItem;
    }
}
