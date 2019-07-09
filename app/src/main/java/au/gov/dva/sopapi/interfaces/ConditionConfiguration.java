package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.processingrules.ApplicableRuleConfigurationImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
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

    default ImmutableSet<ApplicableRuleConfiguration> getApplicableRuleConfigurations(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace)
    {

        long numberOfServiceBranches = serviceHistory.getServices().stream().map(s -> s.getBranch()).distinct().count();

        if (numberOfServiceBranches <= 1) {

            Optional<Rank> relevantRank = ProcessingRuleFunctions.getCFTSRankProximateToDate(serviceHistory.getServices(), condition.getStartDate(), caseTrace);
            Optional<Service> serviceDuringWhichConditionStarts = ProcessingRuleFunctions.identifyCFTSServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(), condition.getStartDate(), caseTrace);
            if (!relevantRank.isPresent() || !serviceDuringWhichConditionStarts.isPresent()) {
                return ImmutableSet.of();
            }

            // rh config is mandatory
            Optional<RHRuleConfigurationItem> rhRuleConfigurationItem = this.getRHRuleConfigurationFor(relevantRank.get(), serviceDuringWhichConditionStarts.get().getBranch());
            if (!rhRuleConfigurationItem.isPresent())
                return ImmutableSet.of();

            // bop config can be left out
            Optional<BoPRuleConfigurationItem> boPRuleConfigurationItem = this.getBoPRuleConfigurationFor(relevantRank.get(), serviceDuringWhichConditionStarts.get().getBranch());

            return ImmutableSet.of(new ApplicableRuleConfigurationImpl(
                    rhRuleConfigurationItem.get(),
                    boPRuleConfigurationItem)
            );
        }

        else {
            // find all the configurations where the
            //
        }

        return null;
    }


}
