package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoError;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;

import java.util.MissingFormatArgumentException;
import java.util.Optional;

public class RuleConfigRepositoryUtils {
    public static Optional<RHRuleConfigurationItem> getRelevantRHConfiguration(String conditionName, Rank rank, ServiceBranch serviceBranch, RuleConfigurationRepository ruleConfigurationRepository)
    {
        Optional<RHRuleConfigurationItem> item = ruleConfigurationRepository.getRHItems().stream()
                .filter(i -> i.getConditionName().contentEquals(conditionName))
                .filter(i -> i.getRank() == rank)
                .filter(i -> i.getServiceBranch() == serviceBranch)
                .findFirst();
        return item;
    }

    public static Optional<BoPRuleConfigurationItem> getRelevantBoPConfiguration(String conditionName, Rank rank, ServiceBranch serviceBranch, RuleConfigurationRepository ruleConfigurationRepository) {
        Optional<BoPRuleConfigurationItem> item = ruleConfigurationRepository.getBoPItems().stream()
                .filter(i -> i.getConditionName().contentEquals(conditionName))
                .filter(i -> i.getRank() == rank)
                .filter(i -> i.getServiceBranch() == serviceBranch)
                .findFirst();
        return item;
    }

    public static Optional<RuleConfigurationItem> getApplicableRuleConfigurationItem(StandardOfProof standardOfProof, String conditionName, Rank rank, ServiceBranch serviceBranch, RuleConfigurationRepository ruleConfigurationRepository)
    {
        if (standardOfProof == StandardOfProof.ReasonableHypothesis)
        {
             Optional<RHRuleConfigurationItem> r = getRelevantRHConfiguration(conditionName,rank,serviceBranch,ruleConfigurationRepository);
             return r.isPresent() ? Optional.of(r.get()) : Optional.empty();

        }
        else {
            Optional<BoPRuleConfigurationItem> r = getRelevantBoPConfiguration(conditionName,rank,serviceBranch,ruleConfigurationRepository);
            return r.isPresent() ? Optional.of(r.get()) : Optional.empty();
        }
    }

    public static boolean containsConfigForCondition(String conditionName, RuleConfigurationRepository ruleConfigurationRepository)
    {
        return ruleConfigurationRepository.getRHItems()
                .stream()
                .anyMatch(i -> i.getConditionName().contentEquals(conditionName));
    }


}
