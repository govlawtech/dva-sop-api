package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface Service {
    ServiceBranch getBranch();
    EmploymentType getEmploymentType();
    Rank getRank();
    LocalDate getStartDate();
    Optional<LocalDate> getEndDate();
    ImmutableSet<Deployment> getDeployments();
}
