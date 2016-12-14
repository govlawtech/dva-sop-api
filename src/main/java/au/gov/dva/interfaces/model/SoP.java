package au.gov.dva.interfaces.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;

public interface SoP {
    String getRegisterId();
    InstrumentNumber getInstrumentNumber();
    String getCitation();
    ImmutableList<Factor> getAggravationFactors();
    ImmutableList<Factor> getOnsetFactors();
    LocalDate getEffectiveFromDate();
    StandardOfProof getStandardOfProof();
    ImmutableSet<ICDCode> getICDCodes();
    String getConditionName();
}
