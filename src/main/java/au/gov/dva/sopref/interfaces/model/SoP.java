package au.gov.dva.sopref.interfaces.model;

import com.fasterxml.jackson.databind.JsonNode;
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
    ImmutableList<ICDCode> getICDCodes();
}
