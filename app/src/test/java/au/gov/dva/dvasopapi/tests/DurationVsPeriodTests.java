package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopapi.DateTimeUtils;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class DurationVsPeriodTests {

    @Test
    public void TenYearsPeriodVsDuration()
    {
        LocalDate start = LocalDate.of(2004,7,1);
        LocalDate end = LocalDate.of(2014,6,30);

        long periodYears =  ChronoUnit.YEARS.between(start,end);
        System.out.println("Period years: " + periodYears);

        long periodDays = ChronoUnit.DAYS.between(start,end);
        System.out.println("Period days: " + periodDays);

        Duration duration = Duration.between(
                start.atStartOfDay(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE)),
                end.atStartOfDay(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE)));

        System.out.print("Duration days: " + duration.toDays());
    }

    @Test
    public void DaysAroundDaylightSavingSpringForward()
    {
        LocalDate start = LocalDate.of(2017,4,3);
        LocalDate end = LocalDate.of(2017,10,2);

        long periodDays = ChronoUnit.DAYS.between(start,end);
        System.out.println("Period days: " + periodDays);

        Duration duration = Duration.between(
                start.atStartOfDay(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE)),
                end.atStartOfDay(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE))
        );
        long durationDays = duration.toDays();
        System.out.println("Duration days: " + durationDays);
    }

}
