package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class AcceptedSequalaeRequestDto {

    @JsonProperty(value = "conditions", required = true)
    private final List<AcceptedSequalaeRequestConditionDto> _conditions;

    public List<AcceptedSequalaeRequestConditionDto> getConditions() {
        return _conditions;
    }

    public AcceptedSequalaeRequestDto(@JsonProperty("conditions") List<AcceptedSequalaeRequestConditionDto> conditions)
    {
        _conditions = conditions;
    }

    public static AcceptedSequalaeRequestDto fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            AcceptedSequalaeRequestDto acceptedSequalaeRequestConditionDto = objectMapper.readValue(json,AcceptedSequalaeRequestDto.class);
            return acceptedSequalaeRequestConditionDto;
        } catch (IOException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
    }
}
