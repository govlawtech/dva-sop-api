package au.gov.dva.sopapi.dtos.sopsupport;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.ServiceHistoryDto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public class SopSupportRequestDto {

    @JsonProperty(value = "condition", required = true)
    private final ConditionDto _conditionDto;

    @JsonProperty(value = "serviceHistory", required = true)
    private final ServiceHistoryDto _serviceHistoryDto;

    @JsonCreator
    public SopSupportRequestDto(@JsonProperty("condition") ConditionDto _conditionDto, @JsonProperty("serviceHistory") ServiceHistoryDto _serviceHistoryDto) {
        this._conditionDto = _conditionDto;
        this._serviceHistoryDto = _serviceHistoryDto;
    }

    public static SopSupportRequestDto fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            SopSupportRequestDto sopSupportRequestDto = objectMapper.readValue(json,SopSupportRequestDto.class);
            return sopSupportRequestDto;
        } catch (IOException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
    }

    public String toJsonString()
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING,true);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
    }


    public ConditionDto get_conditionDto() {
        return _conditionDto;
    }

    public ServiceHistoryDto get_serviceHistoryDto() {
        return _serviceHistoryDto;
    }
}
