package au.gov.dva.sopref.interfaces.model;

import java.time.LocalDate;
import java.util.Optional;

public interface Condition {
    String getName();
    String getICDCode();
    String getType();
    LocalDate getOnsetStartDate();
    Optional<LocalDate> getOnsetEndDate();
    Optional<LocalDate> getAggravationStartDate();
    Optional<LocalDate> getAggravationEndDate();
}
