package au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors;

import au.gov.dva.sopapi.interfaces.IntervalSelector;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import scala.Int;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FixedYearsPeriodSelector implements IntervalSelector {

    private Integer fixedCalendarYearsBeforeEnd;

    public FixedYearsPeriodSelector(Integer fixedCalendarYearsBeforeEnd)
    {
        this.fixedCalendarYearsBeforeEnd = fixedCalendarYearsBeforeEnd;

    }


    @Override
    public Interval getInterval(ServiceHistory serviceHistory, LocalDate upperBoundaryInclusive) {
        LocalDate upperLessYears = upperBoundaryInclusive.minusYears(fixedCalendarYearsBeforeEnd);
        return new Interval(upperLessYears,upperBoundaryInclusive);
    }
}

