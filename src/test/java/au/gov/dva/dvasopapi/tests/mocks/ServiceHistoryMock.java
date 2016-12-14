package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.interfaces.model.*;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.util.Optional;

public class ServiceHistoryMock implements ServiceHistory {

    public LocalDate getEnlistmentDate() {
        return LocalDate.of(2004, 7, 1);
    }

    public LocalDate getSeparationDate() {
        return LocalDate.of(2010, 6, 30);
    }

    public LocalDate getHireDate() {
        return LocalDate.of(2004, 7, 20);
    }

    public ImmutableSet<Service> getServices() {
        return ImmutableSet.of(new Service() {
            @Override
            public String getName() {
                return "Royal Australian Air Force";
            }

            @Override
            public String getType() {
                return "Regular/Permanent Force";
            }

            @Override
            public LocalDate getStartDate() {
                return LocalDate.of(2004, 8, 1);
            }

            @Override
            public LocalDate getEndDate() {
                return LocalDate.of(2010, 6, 30);
            }

            @Override
            public ImmutableSet<Deployment> getDeployments() {
                return ImmutableSet.of(new Deployment() {

                    @Override
                    public Operation getOperation() {
                        return new Operation() {
                            @Override
                            public String getName() {
                                return "Operation WARDEN";
                            }

                            @Override
                            public ServiceType getServiceType() {
                                return ServiceType.WARLIKE;
                            }

                            @Override
                            public LocalDate getStartDate() {
                                return null;
                            }

                            @Override
                            public Optional<LocalDate> getEndDate() {
                                return null;
                            }
                        };
                    }

                    @Override
                    public LocalDate getStartDate() {
                        return LocalDate.of(2006, 3, 1);
                    }

                    @Override
                    public Optional<LocalDate> getEndDate() {
                            return Optional.of(LocalDate.of(2006, 12, 31));
                    }



                });
            }
        });
    }
}
