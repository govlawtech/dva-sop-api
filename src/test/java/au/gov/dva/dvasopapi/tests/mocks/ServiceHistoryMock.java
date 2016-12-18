package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static au.gov.dva.dvasopapi.tests.TestUtils.odtOf;

public class ServiceHistoryMock implements ServiceHistory {

    public OffsetDateTime getEnlistmentDate() {
        return odtOf(2004, 7, 1);
    }

    public OffsetDateTime getSeparationDate() {
        return odtOf(2016,6,30);
    }

    public OffsetDateTime getHireDate() {
        return odtOf(2004, 7, 20);
    }

    public ImmutableSet<Service> getServices() {
        return ImmutableSet.of(new Service() {
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
                return OffsetDateTime.of(2004, 8, 1,0,0,0,0,ZoneOffset.UTC);
            }

            @Override
            public Optional<OffsetDateTime> getEndDate() {
                return Optional.of(OffsetDateTime.of(2010, 6, 30,0,0,0,0,ZoneOffset.UTC));
            }

            @Override
            public ImmutableSet<Deployment> getDeployments() {
                return ImmutableSet.of(new Deployment() {


                    @Override
                    public String getOperationName() {
                        return "Operation WARDEN";
                    }

                    @Override
                    public OffsetDateTime getStartDate() {
                        return OffsetDateTime.of(2006, 3, 1,0,0,0,0, ZoneOffset.UTC);
                    }

                    @Override
                    public Optional<OffsetDateTime> getEndDate() {
                            return Optional.of(OffsetDateTime.of(2006, 12, 31,0,0,0,0,ZoneOffset.UTC));
                    }
                });
            }
        });
    }
}
