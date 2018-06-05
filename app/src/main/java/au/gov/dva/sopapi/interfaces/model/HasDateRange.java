package au.gov.dva.sopapi.interfaces.model;

import java.time.LocalDate;
import java.util.Optional;

public interface HasDateRange {
    LocalDate getStartDate();
    Optional<LocalDate> getEndDate();
}
