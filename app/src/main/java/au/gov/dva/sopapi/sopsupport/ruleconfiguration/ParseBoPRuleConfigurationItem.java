package au.gov.dva.sopapi.sopsupport.ruleconfiguration;

import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;

import java.util.Optional;

public final class ParseBoPRuleConfigurationItem extends ParsedRuleConfigurationItem implements BoPRuleConfigurationItem {
    public ParseBoPRuleConfigurationItem(String conditionName, String instrumentId, String factorRefs, String serviceBranch, String rank, String cftsDays) {
        super(conditionName, instrumentId, factorRefs, serviceBranch, rank, cftsDays);
    }


}
