package au.gov.dva.sopapi.interfaces.model;

import java.time.OffsetDateTime;

public interface AggravatedCondition extends Condition
{
    OffsetDateTime getInitialInjuryStartDate();

    OffsetDateTime getInitialInjuryEndDate();
}
