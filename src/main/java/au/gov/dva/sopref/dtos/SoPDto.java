package au.gov.dva.sopref.dtos;

import au.gov.dva.interfaces.model.Factor;
import au.gov.dva.interfaces.model.IncidentType;
import au.gov.dva.interfaces.model.SoP;
import au.gov.dva.interfaces.model.StandardOfProof;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

public class SoPDto {


    @JsonProperty("registerId")
    private final String _registerId;

    @JsonProperty("citation")
    private final String _citation;

    @JsonProperty("instrumentNumber")
    private final String _instrumentNumber;

    @JsonProperty("factors")
    private final List<FactorDto> _factors;

    public SoPDto(String registerId, String citation, String instrumentNumber, List<FactorDto> factors) {
        _registerId = registerId;
        _citation = citation;
        _instrumentNumber = instrumentNumber;
        _factors = factors;
    }


    public static SoPDto fromSop(SoP sop, StandardOfProof standardOfProof, IncidentType incidentType) {


        ImmutableList<Factor> factorsToInclude = (sop.getStandardOfProof() != standardOfProof) ? ImmutableList.of() : (incidentType == IncidentType.Aggravation) ?
                sop.getAggravationFactors() : ((incidentType == IncidentType.Onset) ?
                sop.getOnsetFactors() : ImmutableList.of());

        List<FactorDto> factorDtos = factorsToInclude.stream().map(f -> FactorDto.fromFactor(f)).collect(Collectors.toList());

        String instrumentNumber = String.format("%d/%d", sop.getInstrumentNumber().getNumber(), sop.getInstrumentNumber().getYear());

        return new SoPDto(sop.getRegisterId(),sop.getCitation(),instrumentNumber,factorDtos);

    }


}
