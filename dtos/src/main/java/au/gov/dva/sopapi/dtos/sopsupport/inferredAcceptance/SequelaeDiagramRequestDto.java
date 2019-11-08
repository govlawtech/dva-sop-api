package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class SequelaeDiagramRequestDto {

    @JsonProperty(value = "acceptedConditions", required = true)
    private final List<String> _acceptedConditions;

    @JsonProperty(value = "diagnosedConditions", required = true)
    private final List<String> _diagnosedConditions;

    public SequelaeDiagramRequestDto(@JsonProperty("acceptedConditions") List<String> acceptedConditions, @JsonProperty("diagnosedConditions") List<String> diagnosedConditions)
    {
        _acceptedConditions = acceptedConditions;
        _diagnosedConditions = diagnosedConditions;
    }

    public List<String> get_acceptedConditions() {
        return _acceptedConditions;
    }

    public List<String> get_diagnosedConditions() {
        return _diagnosedConditions;
    }


    public static SequelaeDiagramRequestDto fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            SequelaeDiagramRequestDto sequelaeDiagramRequestDto = objectMapper.readValue(json,SequelaeDiagramRequestDto.class);
            return sequelaeDiagramRequestDto;
        } catch (IOException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
    }

}
