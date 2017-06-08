package au.gov.dva.sopapi.sopsupport.processingrules;

import java.time.OffsetDateTime;

public class Interval {

    private final OffsetDateTime start;
    private final OffsetDateTime end;

    public Interval(OffsetDateTime start, OffsetDateTime end)
    {

        this.start = start;
        this.end = end;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }
}
