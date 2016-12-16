package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.util.Optional;

public interface Condition {

    SoPPair getSopPair();
    LocalDate getOnsetStartDate();
    Optional<LocalDate> getOnsetEndDate();
    ImmutableSet<Factor> getApplicableFactors(ServiceHistory serviceHistory);
    ImmutableSet<Factor> getSatisfiedFactors(ServiceHistory serviceHistory);
    SoP getApplicableSop();
}

