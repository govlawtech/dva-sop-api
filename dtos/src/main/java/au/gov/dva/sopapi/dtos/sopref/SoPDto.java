package au.gov.dva.sopapi.dtos.sopref;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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




}
