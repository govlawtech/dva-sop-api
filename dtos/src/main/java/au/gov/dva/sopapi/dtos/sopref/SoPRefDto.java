package au.gov.dva.sopapi.dtos.sopref;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoError;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.util.List;

public class SoPRefDto {

    @JsonProperty("applicableFactors")
    private final List<SoPDto> _sops;

    public SoPRefDto(@JsonProperty("applicableFactors") List<SoPDto> soPDtos)
    {
        _sops = soPDtos;
    }

    public static String toJsonString(SoPRefDto soPRefDto)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        String jsonString = null;

        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(soPRefDto);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiDtoError(e);
        }

        return jsonString;
    }

    public static SoPRefDto fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        try {
            SoPRefDto soPRefDto = objectMapper.readValue(json, SoPRefDto.class);
            return soPRefDto;
        } catch (IOException e) {
            throw new DvaSopApiDtoError(e);
        }
    }
}
