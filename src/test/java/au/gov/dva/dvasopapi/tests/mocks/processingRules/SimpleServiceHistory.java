package au.gov.dva.dvasopapi.tests.mocks.processingRules;


import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.Optional;

import static au.gov.dva.dvasopapi.tests.TestUtils.actOdtOf;

public class SimpleServiceHistory {

    // 31 days of peacetime service
    // 122 days of operation service


    public static ServiceHistory get()
    {
        OffsetDateTime startDate = actOdtOf(2004,7,1);
        OffsetDateTime endDate = actOdtOf(2005,6,30);

        return new ServiceHistory() {

            @Override
            public OffsetDateTime getHireDate() {
                return startDate;
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
                                return startDate;
                            }

                            @Override
                            public Optional<OffsetDateTime> getEndDate() {
                                return Optional.of(endDate);
                            }

                            @Override
                            public ImmutableSet<Deployment> getDeployments() {
                                return ImmutableSet.of(
                                        new Deployment() {
                                            @Override
                                            public String getOperationName() {
                                                return "Peace is Our Profession";
                                            }

                                            @Override
                                            public OffsetDateTime getStartDate() {
                                                return actOdtOf(2004,8,1);
                                            }

                                            @Override
                                            public Optional<OffsetDateTime> getEndDate() {
                                                return Optional.of(actOdtOf(2004,9,1).minusDays(1));
                                            }
                                        },

                                        new Deployment() {
                                            @Override
                                            public String getOperationName() {
                                                return "Operation HERRICK";
                                            }

                                            @Override
                                            public OffsetDateTime getStartDate() {
                                                return actOdtOf(2004,9,1);
                                            }

                                            @Override
                                            public Optional<OffsetDateTime> getEndDate() {
                                                return Optional.of(actOdtOf(2004,12,31));
                                            }
                                        }
                                );
                            }
                        }
                );
            }
        };
    }
}
