package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.ServiceType;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class NonWarlikeServiceDeterminationMock implements ServiceDetermination {

    @Override
    public String getRegisterId() {
        return "MOCK_NON_WARLIKE_REGISTER_ID";

    }

    @Override
    public String getCitation() {
        return "Mock non warlike citation";
    }

    @Override
    public OffsetDateTime getCommencementDate() {
        return OffsetDateTime.of(2000, 1, 1, 1, 1, 1, 1, ZoneOffset.ofHoursMinutes(1, 1));
    }

    @Override
    public ImmutableList<Operation> getOperations() {
        return ImmutableList.of(
                new Operation() {
                    @Override
                    public String getName() {
                        return "Duck Rescue";
                    }

                    @Override
                    public ServiceType getServiceType() {
                        return ServiceType.NON_WARLIKE;
                    }

                    @Override
                    public LocalDate getStartDate() {
                        return LocalDate.of(2010, 1, 1);
                    }

                    @Override
                    public Optional<LocalDate> getEndDate() {
                        return Optional.of(LocalDate.of(2011, 1, 1));
                    }
                }

        );
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.NON_WARLIKE;
    }
}
