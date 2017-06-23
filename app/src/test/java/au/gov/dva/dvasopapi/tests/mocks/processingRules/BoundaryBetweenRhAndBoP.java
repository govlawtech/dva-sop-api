package au.gov.dva.dvasopapi.tests.mocks.processingRules;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static au.gov.dva.dvasopapi.tests.TestUtils.odtOf;

public class BoundaryBetweenRhAndBoP implements ServiceHistory {



    @Override
    public LocalDate getHireDate() {
        return LocalDate.of(2004,7,20);
    }

    @Override
    public ImmutableSet<Service> getServices() {
        return ImmutableSet.of(
                new Service() {
                    @Override
                    public ServiceBranch getBranch() {
                        return ServiceBranch.ARMY;
                    }

                    @Override
                    public EmploymentType getEmploymentType() {
                        return EmploymentType.CFTS;
                    }

                    @Override
                    public Rank getRank() {
                        return Rank.OtherRank;
                    }

                    @Override
                    public LocalDate getStartDate() {
                        return LocalDate.of(2004,7,1);
                    }

                    @Override
                    public Optional<LocalDate> getEndDate() {
                        return Optional.of(LocalDate.of(2004,6,30));
                    }

                    @Override
                    public ImmutableSet<Deployment> getDeployments() {
                        return null;
                    }
                }
        );
    }
}
