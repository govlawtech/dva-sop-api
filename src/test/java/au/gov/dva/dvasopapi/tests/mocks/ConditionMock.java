package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopref.interfaces.model.Condition;
import au.gov.dva.sopref.interfaces.model.ConditionType;
import au.gov.dva.sopref.interfaces.model.SoP;

import java.time.LocalDate;
import java.util.Optional;

public class ConditionMock implements Condition {


    @Override
    public SoP getSoP() {
        return new MockLumbarSpondylosisSop();
    }

    public LocalDate getOnsetStartDate() {
        return LocalDate.of(2004, 11, 1);
    }

    public Optional<LocalDate> getOnsetEndDate() {
        return Optional.of(LocalDate.of(2004, 11, 7));
    }

}