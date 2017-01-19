package au.gov.dva.sopapi;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final String TZDB_REGION_CODE = "Australia/ACT";

    public static OffsetDateTime toMidnightAmNextDay(OffsetDateTime prevDay)
    {
        OffsetDateTime sameTimeNextDay = prevDay.plusDays(1);
        LocalDate nextDayLocalDate = sameTimeNextDay.toLocalDate();
        return OffsetDateTime.of(nextDayLocalDate,LocalTime.MIDNIGHT,prevDay.getOffset());
    }

    public static LocalDate toPrevDay(OffsetDateTime offsetDateTime)
    {
        OffsetDateTime prevDay = offsetDateTime.minusDays(1);
        return prevDay.toLocalDate();
    }

    public static OffsetDateTime localDateActTimeToNextDayMidnightAm(String input)
    {
        return localDateStringToActMidnightOdt(input).plusDays(1);
    }

    public static OffsetDateTime localDateStringToActMidnightOdt(String input)
    {
        LocalDate localDate = LocalDate.parse(input,DateTimeFormatter.ISO_LOCAL_DATE);
        return localDateToMidnightACTDate(localDate);
    }

    public static OffsetDateTime stringToOffsetDateTime(String input)
    {
        String withAssumedTime = input.matches("^[0-9]{4,4}-[0-9]{2,2}-[0-9]{2,2}Z$") ?
                input.replaceFirst("Z","T00:00:00Z") : input;
        OffsetDateTime parsed = OffsetDateTime.parse(withAssumedTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return parsed;
    }

    public static OffsetDateTime localDateToMidnightACTDate(LocalDate localDate)
    {
        ZonedDateTime zonedToACT = ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.of(TZDB_REGION_CODE));
        return OffsetDateTime.from(zonedToACT);
    }

    public static LocalDate odtToActLocalDate(OffsetDateTime offsetDateTime)
    {
        ZonedDateTime zonedDateTime =  offsetDateTime.atZoneSameInstant(ZoneId.of(TZDB_REGION_CODE));
        return LocalDate.of(zonedDateTime.getYear(),zonedDateTime.getMonth(),zonedDateTime.getDayOfMonth());
    }

    public static String localDateToUtcLocalDate(LocalDate localDate)
    {
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }


}
