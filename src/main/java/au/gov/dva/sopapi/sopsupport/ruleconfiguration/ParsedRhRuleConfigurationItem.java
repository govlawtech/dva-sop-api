package au.gov.dva.sopapi.sopsupport.ruleconfiguration;

import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;

import java.util.Optional;

public final class ParsedRhRuleConfigurationItem extends ParsedRuleConfigurationItem implements RHRuleConfigurationItem {
    private int rhOperationalDaysRequired;
    private int rhOperationalTestPeriodInYears;

    public ParsedRhRuleConfigurationItem(String conditionName, String instrumentId, String factorRefs, String serviceBranch, String rank, String cftsWeeks, Optional<String> accumRate, Optional<String> accumUnit, String rhOperationalDaysRequired, String rhOperationalTestPeriodInYears) {
        super(conditionName, instrumentId, factorRefs, serviceBranch, rank, cftsWeeks, accumRate, accumUnit);

        this.rhOperationalDaysRequired = super.toInt(rhOperationalDaysRequired, "Cannot determine days of operational service required for RH from");
        this.rhOperationalTestPeriodInYears = super.toInt(rhOperationalTestPeriodInYears, "Cannot determine the test period for operational service from");
    }

    @Override
    public int getRequiredDaysOfOperationalService() {
        return rhOperationalDaysRequired;
    }

    @Override
    public int getYearsLimitForOperationalService() {
        return rhOperationalTestPeriodInYears;
    }
}