package au.gov.dva.interfaces.model;


import java.time.LocalDate;
import java.util.Optional;

public interface Deployment {
  Operation getOperation();
  LocalDate getStartDate();
  Optional<LocalDate> getEndDate();
}
