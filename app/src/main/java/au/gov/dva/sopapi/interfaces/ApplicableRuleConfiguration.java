package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.StandardOfProof;

import java.util.Optional;

public interface ApplicableRuleConfiguration {
    RHRuleConfigurationItem getRHRuleConfigurationItem();
    Optional<BoPRuleConfigurationItem> getBopRuleConfigurationItem();
    default Optional<? extends  RuleConfigurationItem> getRuleConfigurationForStandardOfProof(StandardOfProof standardOfProof) {
        if (standardOfProof == StandardOfProof.ReasonableHypothesis)
        {
            return Optional.of(getRHRuleConfigurationItem());
        }
        else return getBopRuleConfigurationItem();
    }
}