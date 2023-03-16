package au.gov.dva.sopapi.interfaces.model;

import java.time.LocalDate;
import java.util.Optional;

public interface MaybeOpenEndedInterval {
    LocalDate getStartDate();
    Optional<LocalDate> getEndDate();

    default boolean isValid()
    {
        return !getEndDate().isPresent() || !getEndDate().get().isBefore(getStartDate());
    }

}
