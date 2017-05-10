package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.ServiceBranch;
import com.google.common.collect.ImmutableSet;

public interface ServiceBranchSpecification {
    ServiceBranch getServiceBranch();
    ImmutableSet<RankSpecification> getRankSpecifications();
}
