package au.gov.dva.sopref.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class SoPRefDto {

    @JsonProperty("applicableFactors")
    private final List<SoPDto> _sops;

    public SoPRefDto(List<SoPDto> soPDtos)
    {
        _sops = soPDtos;
    }
}
