package au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors;

import au.gov.dva.sopapi.interfaces.IntervalSelector;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;

import java.time.LocalDate;

public class FixedDaysPeriodSelector implements IntervalSelector {

    private Integer fixedCalendarDaysBeforeEnd;

    public FixedDaysPeriodSelector(Integer fixedCalendarDaysBeforeEnd)
    {
        this.fixedCalendarDaysBeforeEnd = fixedCalendarDaysBeforeEnd;
    }

    @Override
    public Interval getInterval(ServiceHistory serviceHistory, LocalDate upperBoundaryInclusive) {
        LocalDate upperLessDays = upperBoundaryInclusive.minusDays(fixedCalendarDaysBeforeEnd);
        return new Interval(upperLessDays,upperBoundaryInclusive);
    }
}
