package au.gov.dva.sopapi.interfaces.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by mc on 27/06/17.
 */
public interface HasDateRange {
    LocalDate getStartDate();
    Optional<LocalDate> getEndDate();
}
