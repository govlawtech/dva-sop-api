package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.StandardOfProof;

public interface ApplicableRuleConfiguration {
    RHRuleConfigurationItem getRHRuleConfigurationItem();
    BoPRuleConfigurationItem getBopRuleConfigurationItem();
    default RuleConfigurationItem getRuleConfigurationForStandardOfProof(StandardOfProof standardOfProof) {
        if (standardOfProof == StandardOfProof.ReasonableHypothesis)
        {
            return getRHRuleConfigurationItem();
        }
        else return getBopRuleConfigurationItem();
    }
}
