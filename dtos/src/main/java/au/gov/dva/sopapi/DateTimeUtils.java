package au.gov.dva.sopapi;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final String TZDB_REGION_CODE = "Australia/ACT";

    public static OffsetDateTime toMidnightAmNextDay(OffsetDateTime timeDuringPrevDay)
    {
        OffsetDateTime sameTimeNextDay = timeDuringPrevDay.plusDays(1);
        LocalDate nextDayLocalDate = sameTimeNextDay.toLocalDate();
        return OffsetDateTime.of(nextDayLocalDate,LocalTime.MIDNIGHT,timeDuringPrevDay.getOffset());
    }

    public static OffsetDateTime toMightnightAmThisDay(OffsetDateTime timeDuringDay)
    {
        return OffsetDateTime.of(timeDuringDay.toLocalDate(),LocalTime.MIDNIGHT,timeDuringDay.getOffset());
    }

    public static LocalDate toPrevDay(OffsetDateTime offsetDateTime)
    {
        OffsetDateTime prevDay = offsetDateTime.minusDays(1);
        return prevDay.toLocalDate();
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




}
