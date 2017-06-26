package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.dvasopapi.tests.TestUtils;
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

import static au.gov.dva.dvasopapi.tests.TestUtils.actOdtOf;

public class ExtensiveServiceHistoryMock implements ServiceHistory {


    @Override
    public OffsetDateTime getHireDate() {
        return null;
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
                        return actOdtOf(1993,1,1);
                    }

                    @Override
                    public Optional<OffsetDateTime> getEndDate() {
                        return Optional.of(actOdtOf(2016,7,30));
                    }

                    @Override
                    public ImmutableSet<Deployment> getDeployments() {
                        try {
                            return ImmutableSet.copyOf(TestUtils.getTestDeployments());
                        }
                        catch (Exception e) {
                           return null;
                        }
                    }
                }
        );


    }
}
