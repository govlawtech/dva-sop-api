package au.gov.dva.sopref.interfaces.model;

import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.util.Optional;

public interface ServiceDetermination {
    String getRegisterId();
    String getCitation();
    LocalDate getCommencementDate();
    ImmutableList<Operation> getOperations();
    ServiceType getServiceType();
}
