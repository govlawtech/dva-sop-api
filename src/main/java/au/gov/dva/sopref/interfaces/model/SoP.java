package au.gov.dva.sopref.interfaces.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;

public interface SoP {
    String getRegisterId();
    ImmutableSet<Factor> getAggravationFactors();
    ImmutableSet<Factor> getOnsetFactors();
    LocalDate getCommencementDate();
    JsonNode toJson();
}
