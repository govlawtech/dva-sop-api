package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.interfaces.model.*;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.util.Optional;

public class ConditionMock implements Condition {

    @Override
    public SoPPair getSopPair() {
        return new SoPPair(null,new MockLumbarSpondylosisSop());
    }

    public LocalDate getOnsetStartDate() {
        return LocalDate.of(2004, 11, 1);
    }

    public Optional<LocalDate> getOnsetEndDate() {
        return Optional.of(LocalDate.of(2004, 11, 7));
    }

    @Override
    public ImmutableSet<Factor> getApplicableFactors(ServiceHistory serviceHistory) {
        return null;
    }

    @Override
    public ImmutableSet<Factor> getSatisfiedFactors(ServiceHistory serviceHistory) {
        return null;
    }

    @Override
    public SoP getApplicableSop() {
        return null;
    }

}