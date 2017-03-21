package au.gov.dva.sopapi.interfaces;

public interface RHRuleConfigurationItem extends RuleConfigurationItem {
    int getRequiredDaysOfOperationalService();
    int getYearsLimitForOperationalService();
}
