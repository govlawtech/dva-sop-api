package au.gov.dva.interfaces.model;


import java.time.LocalDate;
import java.util.Optional;

public interface Operation {
   String getName();
   ServiceType getServiceType();
   LocalDate getStartDate();
   Optional<LocalDate> getEndDate();
}


