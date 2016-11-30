package au.gov.dva.sopref.interfaces.model;

import java.time.LocalDate;
import java.util.Optional;

public interface Condition {
    String getName();
    String getICDCode();
    ConditionType getType();
    LocalDate getOnsetStartDate();
    // todo: sub interface for aggravated condition
    Optional<LocalDate> getOnsetEndDate();
    Optional<LocalDate> getAggravationStartDate();
    Optional<LocalDate> getAggravationEndDate();
}

