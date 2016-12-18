package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.interfaces.ProcessingRule;

import java.time.OffsetDateTime;

public interface Condition {

    SoPPair getSopPair();
    OffsetDateTime getStartDate();
    OffsetDateTime getEndDate();
    ProcessingRule getProcessingRule();
}

