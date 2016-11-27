package au.gov.dva.sopref.interfaces.model;


import java.time.LocalDate;
import java.util.Optional;

public interface Operation {
   String getName();
   LocalDate getStartDate();
   Optional<LocalDate> getEndDate();
}


