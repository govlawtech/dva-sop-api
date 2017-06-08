package au.gov.dva.sopapi.sopsupport.ruleconfiguration;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;

import java.util.Optional;

public final class ParsedRhRuleConfigurationItem extends ParsedRuleConfigurationItem implements RHRuleConfigurationItem {
    private final  int _rhOperationalDaysRequired;
    private final Optional<Integer> _operationalTestPeriod;


    public ParsedRhRuleConfigurationItem(String conditionName, String instrumentId, String factorRefs, String serviceBranch, String rank, String cftsWeeks, Optional<String> accumRate, Optional<String> accumUnit, String rhOperationalDaysRequired, Optional<String> rhOperationalTestPeriodInYears) {
        super(conditionName, instrumentId, factorRefs, serviceBranch, rank, cftsWeeks, accumRate, accumUnit);

        _rhOperationalDaysRequired = super.toIntOrError(rhOperationalDaysRequired, "Cannot determine days of operational service required for RH from ");

        if (rhOperationalTestPeriodInYears.isPresent()) {
            Integer value = super.toIntOrError(rhOperationalTestPeriodInYears.get(), "Cannot determine the test period for operational service from ");
            _operationalTestPeriod = Optional.of(value);
        }
        else {
            _operationalTestPeriod = Optional.empty();
        }

    }

    @Override
    public int getRequiredDaysOfOperationalService() {
        return _rhOperationalDaysRequired;
    }

    @Override
    public Optional<Integer> getYearsLimitForOperationalService() {
        return _operationalTestPeriod;
    }

}