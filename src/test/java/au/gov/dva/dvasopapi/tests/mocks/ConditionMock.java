package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopref.interfaces.model.Condition;
import au.gov.dva.sopref.interfaces.model.ConditionType;

import java.time.LocalDate;
import java.util.Optional;

public class ConditionMock implements Condition {

    public String getName() {
        return "lumbar spondylosis";
    }

    public String getICDCode() {
        return "ICD-10-AM-M51.3";
    }

    public String getType() {
        return ConditionType.AccumulatedOverTime.toString();
    }

    public LocalDate getOnsetStartDate() {
        return LocalDate.of(2004, 11, 1);
    }

    public Optional<LocalDate> getOnsetEndDate() {
        return Optional.of(LocalDate.of(2004, 11, 7));
    }

    public Optional<LocalDate> getAggravationStartDate() {
        return Optional.of(LocalDate.of(2005, 11, 7));
    }

    public Optional<LocalDate> getAggravationEndDate() {
        return Optional.of(LocalDate.of(2005, 11, 7));
    }
}