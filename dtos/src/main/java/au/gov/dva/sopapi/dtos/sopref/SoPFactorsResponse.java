package au.gov.dva.sopapi.dtos.sopref;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SoPFactorsResponse {


    @JsonProperty("registerId")
    private final String _registerId;

    @JsonProperty("citation")
    private final String _citation;

    @JsonProperty("instrumentNumber")
    private final String _instrumentNumber;

    @JsonProperty("factors")
    private final List<FactorDto> _factors;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SoPFactorsResponse(@JsonProperty("registerId") String registerId, @JsonProperty("citation") String citation, @JsonProperty("instrumentNumber") String instrumentNumber, @JsonProperty("factorDtos") List<FactorDto> factorDtos) {
        _registerId = registerId;
        _citation = citation;
        _instrumentNumber = instrumentNumber;
        _factors = factorDtos;
    }


    public String get_registerId() {
        return _registerId;
    }

    public String get_citation() {
        return _citation;
    }

    public String get_instrumentNumber() {
        return _instrumentNumber;
    }

    public List<FactorDto> get_factors() {
        return _factors;
    }
}
