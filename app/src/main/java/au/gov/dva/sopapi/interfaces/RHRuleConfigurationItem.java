package au.gov.dva.sopapi.interfaces;

import java.util.Optional;

public interface RHRuleConfigurationItem extends RuleConfigurationItem {
    int getRequiredDaysOfOperationalService();
    Optional<Integer> getYearsLimitForOperationalService();
}
