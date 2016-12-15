package au.gov.dva.sopapi.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SoPRefDto {

    @JsonProperty("applicableFactors")
    private final List<SoPDto> _sops;

    public SoPRefDto(List<SoPDto> soPDtos)
    {
        _sops = soPDtos;
    }
}
