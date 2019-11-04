package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FactorListDto {

    @JsonProperty(value = "instrumentID")
    private final String _instrumentId;

    @JsonProperty(value = "factors")
    private final List<FactorLinkDto> _factors;

    public FactorListDto(@JsonProperty("instrumentID") String instrumentId, @JsonProperty("factors") List<FactorLinkDto> factors)
    {
        _instrumentId = instrumentId;
        _factors = factors;
    }
}
