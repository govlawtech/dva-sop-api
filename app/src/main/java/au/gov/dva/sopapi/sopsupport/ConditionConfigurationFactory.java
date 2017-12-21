package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.interfaces.ConditionConfiguration;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;

import java.util.Optional;

public class ConditionConfigurationFactory {


    private final RuleConfigurationRepository ruleConfigurationRepository;

    public ConditionConfigurationFactory(RuleConfigurationRepository ruleConfigurationRepository)
    {

        this.ruleConfigurationRepository = ruleConfigurationRepository;
    }


    public Optional<ConditionConfiguration> getConditionConfigurationFor(String conditionName)
    {
        switch (conditionName.toLowerCase())
        {
            case "osteoarthritis" : return Optional.empty();
            default: return ruleConfigurationRepository.getConditionConfigurationFor(conditionName);
        }
    }
}
