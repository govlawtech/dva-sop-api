package au.gov.dva.sopapi.sopsupport.processingrules;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Interval {

    private final LocalDate start;
    private final LocalDate end;


    public Interval(LocalDate start, LocalDate end)
    {

        this.start = start;
        this.end = end;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }
}
