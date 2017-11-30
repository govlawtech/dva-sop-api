package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.ServiceType;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

public class ServiceDeterminationMockOperationLittenOnly implements ServiceDetermination {
    @Override
    public String getRegisterId() {
        return null;
    }

    @Override
    public String getCitation() {
        return null;
    }

    @Override
    public OffsetDateTime getCommencementDate() {
        return null;
    }

    @Override
    public ImmutableList<Operation> getOperations() {
        return ImmutableList.of(new Operation() {

            @Override
            public String getName() {
                return "Litten";
            }

            @Override
            public ServiceType getServiceType() {
                return ServiceType.NON_WARLIKE;
            }

            @Override
            public LocalDate getStartDate() {
                return LocalDate.of(2016,8,31);
            }

            @Override
            public Optional<LocalDate> getEndDate() {
                return Optional.of(LocalDate.of(2016,10,20));
            }
        });
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.NON_WARLIKE;
    }
}
