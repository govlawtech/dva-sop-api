package au.gov.dva.dvasopapi.tests.mocks.processingRules;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static au.gov.dva.dvasopapi.tests.TestUtils.odtOf;

public class BoundaryBetweenRhAndBoP implements ServiceHistory {



    @Override
    public OffsetDateTime getHireDate() {
        return odtOf(2004,7,20);
    }

    @Override
    public ServiceHistory filterServiceHistoryByEvents(List<String> eventList) {
        return this;
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
                        return EmploymentType.CTFS;
                    }

                    @Override
                    public Rank getRank() {
                        return Rank.OtherRank;
                    }

                    @Override
                    public OffsetDateTime getStartDate() {
                        return odtOf(2004,7,1);
                    }

                    @Override
                    public Optional<OffsetDateTime> getEndDate() {
                        return Optional.of(odtOf(2004,6,30));
                    }

                    @Override
                    public ImmutableSet<Deployment> getDeployments() {
                        return null;
                    }
                }
        );
    }
}
