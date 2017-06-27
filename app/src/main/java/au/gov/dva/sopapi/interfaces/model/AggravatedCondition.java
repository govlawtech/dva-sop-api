package au.gov.dva.sopapi.interfaces.model;

import java.time.LocalDate;

public interface AggravatedCondition extends Condition
{
    LocalDate getInitialInjuryStartDate();

    LocalDate getInitialInjuryEndDate();
}
