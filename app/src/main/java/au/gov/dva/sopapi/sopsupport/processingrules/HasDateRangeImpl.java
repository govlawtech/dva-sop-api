package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.model.HasDateRange;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by mc on 27/06/17.
 */
public class HasDateRangeImpl implements HasDateRange {
    private final LocalDate startDate;
    private final Optional<LocalDate> endDate;

    public HasDateRangeImpl(LocalDate startDate, Optional<LocalDate> endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public Optional<LocalDate> getEndDate() {
        return endDate;
    }
}
