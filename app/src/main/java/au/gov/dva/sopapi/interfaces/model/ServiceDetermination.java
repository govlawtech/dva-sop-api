package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableList;

import java.time.OffsetDateTime;

public interface ServiceDetermination {
    String getRegisterId();
    String getCitation();
    OffsetDateTime getCommencementDate();
    ImmutableList<Operation> getOperations();
    ServiceType getServiceType();
}
