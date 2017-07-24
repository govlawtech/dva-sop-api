package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.ApplicableRuleConfiguration;
import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;

public class ApplicableRuleConfigurationImpl implements ApplicableRuleConfiguration {

    private final RHRuleConfigurationItem rhRuleConfigurationItem;
    private final BoPRuleConfigurationItem boPRuleConfigurationItem;

    public ApplicableRuleConfigurationImpl(RHRuleConfigurationItem rhRuleConfigurationItem, BoPRuleConfigurationItem boPRuleConfigurationItem)
    {

        this.rhRuleConfigurationItem = rhRuleConfigurationItem;
        this.boPRuleConfigurationItem = boPRuleConfigurationItem;
    }

    @Override
    public RHRuleConfigurationItem getRHRuleConfigurationItem() {
        return rhRuleConfigurationItem;
    }

    @Override
    public BoPRuleConfigurationItem getBopRuleConfigurationItem() {
        return boPRuleConfigurationItem;
    }
}
