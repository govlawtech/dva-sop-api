package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;

public interface ConditionConfiguration {
    String getConditionName();
    ImmutableSet<RHRuleConfigurationItem> getRHRuleConfigurationItems();
    ImmutableSet<BoPRuleConfigurationItem> getBoPRuleConfigurationItems();

    default Optional<RHRuleConfigurationItem> getRHRuleConfigurationFor(Rank rank, ServiceBranch serviceBranch) {
        return getRHRuleConfigurationItems().stream()
                .filter(ruleConfigurationItem -> ruleConfigurationItem.getRank() == rank)
                .filter(ruleConfigurationItem -> ruleConfigurationItem.getServiceBranch() == serviceBranch)
                .findFirst();
    }

    default Optional<BoPRuleConfigurationItem> getBoPRuleConfigurationFor(Rank rank, ServiceBranch serviceBranch) {
        return getBoPRuleConfigurationItems().stream()
                .filter(ruleConfigurationItem -> ruleConfigurationItem.getRank() == rank)
                .filter(ruleConfigurationItem -> ruleConfigurationItem.getServiceBranch() == serviceBranch)
                .findFirst();
    }

    default Optional<? extends RuleConfigurationItem> getApplicableRuleConfigurationItem(StandardOfProof standardOfProof, Rank rank, ServiceBranch serviceBranch)
    {
        if (standardOfProof == StandardOfProof.ReasonableHypothesis)
        {
            return this.getRHRuleConfigurationFor(rank,serviceBranch);
        }
        else {
           return this.getBoPRuleConfigurationFor(rank,serviceBranch);
        }
    }


}
