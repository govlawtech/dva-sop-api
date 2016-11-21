package au.gov.dva.sopref.interfaces.model;

import java.time.LocalDate;

public interface Condition {
    String getName();
    String getICDCode();
    String getType();
    LocalDate getOnsetStartDate();
    LocalDate getOnsetEndDate();
    LocalDate getAggravationStartDate();
    LocalDate getAggravationEndDate();
}
