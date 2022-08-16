package au.gov.dva.sopapi;

import au.gov.dva.sopapi.interfaces.model.HasDateRange;
import au.gov.dva.sopapi.sopref.datecalcs.Intervals;
import au.gov.dva.sopapi.sopsupport.processingrules.HasDateRangeImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DateTimeUtils {

    public static final String TZDB_REGION_CODE = "Australia/ACT";

    public static OffsetDateTime toMidnightAmNextDayUtc(LocalDate prevDay)
    {
        LocalDate nextDay = prevDay.plusDays(1);
        return OffsetDateTime.of(nextDay,LocalTime.MIDNIGHT,ZoneOffset.UTC);
    }

    public static OffsetDateTime toMidnightAmThisDayUtc(LocalDate localDate)
    {
        return OffsetDateTime.of(localDate,LocalTime.MIDNIGHT,ZoneOffset.UTC);
    }

    public static OffsetDateTime localDateToNextMidnightCanberraTime(String input)
    {
        return localDateStringToActMidnightOdt(input).plusDays(1);
    }

    public static OffsetDateTime localDateToNextMidnightCanberraTime(LocalDate localDate)
    {
        return localDateToLastMidnightCanberraTime(localDate).plusDays(1);
    }

    public static OffsetDateTime localDateStringToActMidnightOdt(String input)
    {
        LocalDate localDate = LocalDate.parse(input,DateTimeFormatter.ISO_LOCAL_DATE);
        return localDateToLastMidnightCanberraTime(localDate);
    }

    public static OffsetDateTime stringToOffsetDateTimeWithAssumptions(String input)
    {
        if (input.matches("^[0-9]{4,4}-[0-9]{2,2}-[0-9]{2,2}$"))
        {
            LocalDate localDate = LocalDate.parse(input,DateTimeFormatter.ISO_LOCAL_DATE);
            return DateTimeUtils.localDateToLastMidnightCanberraTime(localDate);
        }
        else if (input.matches("^[0-9]{4,4}-[0-9]{2,2}-[0-9]{2,2}Z$"))
        {
            String convertedToIsoFormat = input.replaceFirst("Z","T00:00:00Z");
            return OffsetDateTime.parse(convertedToIsoFormat,DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        else {
            OffsetDateTime parsed = OffsetDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return parsed;
        }

    }


    public static OffsetDateTime localDateToLastMidnightCanberraTime(LocalDate localDate)
    {
        ZonedDateTime zonedToACT = ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.of(TZDB_REGION_CODE));
        return OffsetDateTime.from(zonedToACT);
    }


    public static LocalDate odtToActLocalDate(OffsetDateTime offsetDateTime)
    {
        ZonedDateTime zonedDateTime =  offsetDateTime.atZoneSameInstant(ZoneId.of(TZDB_REGION_CODE));
        return LocalDate.of(zonedDateTime.getYear(),zonedDateTime.getMonth(),zonedDateTime.getDayOfMonth());
    }

    public static Boolean OpenEndedTestIntervalOverlapsWithInterval(LocalDate boundaryStartDate, LocalDate boundaryEndDate, LocalDate testStartDate, Optional<LocalDate> testEndDate)
    {
        if (testEndDate.isPresent() && testEndDate.get().isBefore(boundaryStartDate))
            return false;
        if (testStartDate.isAfter(boundaryEndDate))
            return false;
        return true;
    }

    public static List<HasDateRange> flattenDateRanges(List<HasDateRange> toFlatten) {
        if (toFlatten == null || toFlatten.size() == 0) return new ArrayList<>();
        ArrayList<HasDateRange> outputs = new ArrayList();
        ArrayList<HasDateRange> inputs = new ArrayList(toFlatten);
        while (inputs.size() > 1) {
            HasDateRange first = inputs.get(0);
            boolean notOverlapping = true;
            for(int i=1; i < inputs.size(); ++i) {
                HasDateRange next = inputs.get(i);
                notOverlapping = (next.getEndDate().isPresent() && first.getStartDate().isAfter(next.getEndDate().get()))
                        || (first.getEndDate().isPresent() && next.getStartDate().isAfter(first.getEndDate().get()));
                // i.e. if overlapping
                if ( !notOverlapping ) {
                    // use earlier start date
                    LocalDate newStart = first.getStartDate().isBefore(next.getStartDate())
                            ? first.getStartDate() : next.getStartDate();
                    Optional<LocalDate> newEnd = Optional.empty();

                    //
                    if (first.getEndDate().isPresent() && next.getEndDate().isPresent()){
                        newEnd = first.getEndDate().get().isAfter(next.getEndDate().get())
                            ? first.getEndDate() : next.getEndDate();
                    }
                    HasDateRange newDateRange = new HasDateRangeImpl(newStart, newEnd);
                    inputs.remove(i);
                    inputs.remove(0);
                    inputs.add(newDateRange);
                    break;
                }
            }
            if (notOverlapping) {
                // Put in the output array
                outputs.add(first);
                inputs.remove(0);
            }
        }
        outputs.add(inputs.get(0)); // Add the last remaining element
        outputs.sort((s1, s2) -> {
            return s1.getStartDate().compareTo(s2.getStartDate());
        });
        return outputs;
    }
}
