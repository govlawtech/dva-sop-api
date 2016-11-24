package au.gov.dva.sopref.interfaces.model;

import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;

public interface SoP {
    String getRegisterId();
    InstrumentNumber getInstrumentNumber();
    String getCitation();
    ImmutableSet<Factor> getAggravationFactors();
    ImmutableSet<Factor> getOnsetFactors();
    LocalDate getEffectiveFromDate();
    StandardOfProof getStandardOfProof();
}
