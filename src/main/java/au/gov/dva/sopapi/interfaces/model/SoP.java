package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.util.Optional;

public interface SoP {
    String getRegisterId();
    InstrumentNumber getInstrumentNumber();
    String getCitation();
    ImmutableList<Factor> getAggravationFactors();
    ImmutableList<Factor> getOnsetFactors();
    LocalDate getEffectiveFromDate();
    Optional<LocalDate> getEndDate();
    StandardOfProof getStandardOfProof();
    ImmutableSet<ICDCode> getICDCodes();
    String getConditionName();
}
