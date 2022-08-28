package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import com.google.common.collect.ImmutableSet;

public interface Service extends MaybeOpenEndedInterval {
    ServiceBranch getBranch();
    EmploymentType getEmploymentType();
    Rank getRank();
    ImmutableSet<Deployment> getDeployments();
}
