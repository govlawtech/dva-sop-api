package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableList;

import java.time.LocalDate;

public interface ServiceDetermination {
    String getRegisterId();
    String getCitation();
    LocalDate getCommencementDate();
    ImmutableList<Operation> getOperations();
    ServiceType getServiceType();
}
