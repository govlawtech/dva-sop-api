package au.gov.dva.interfaces.model;

import au.gov.dva.sopapi.dtos.StandardOfProof;
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
