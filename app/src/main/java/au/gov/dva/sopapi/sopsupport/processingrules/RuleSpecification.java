package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;

import java.util.Optional;

public interface RuleSpecification {
    Optional<RankSpecification> getSpec(Rank rank, ServiceBranch serviceBranch);
}
