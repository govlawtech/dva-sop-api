package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.ServiceType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class WarlikeServiceDeterminationMock implements ServiceDetermination
{

    @Override
    public String getRegisterId() {
        return "MOCK_WARLIKE_REGISTER_ID";
    }

    @Override
    public String getCitation() {
        return "MOCK_CITATION";
    }

    @Override
    public OffsetDateTime getCommencementDate() {
        return OffsetDateTime.of(2000,1,1,1,1,1,1, ZoneOffset.ofHoursMinutes(1,1));
    }

    @Override
    public ImmutableList<Operation> getOperations() {
        return ImmutableList.of(
                new Operation() {
                    @Override
                    public String getName() {
                        return "Kitty Rescue";
                    }

                    @Override
                    public ServiceType getServiceType() {
                        return ServiceType.WARLIKE;
                    }

                    @Override
                    public LocalDate getStartDate() {
                        return LocalDate.of(2010,1,1);
                    }

                    @Override
                    public Optional<LocalDate> getEndDate() {
                        return Optional.empty();
                    }
                }

        );
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.WARLIKE;
    }
}
