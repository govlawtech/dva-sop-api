package au.gov.dva.sopapi.dtos.sopsupport;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoError;
import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.ServiceHistoryDto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public class RequestDto {

    @JsonProperty(value = "condition", required = true)
    private final ConditionDto _conditionDto;

    @JsonProperty(value = "serviceHistory", required = true)
    private final ServiceHistoryDto _serviceHistoryDto;

    @JsonCreator
    public RequestDto(@JsonProperty("condition") ConditionDto _conditionDto, @JsonProperty("serviceHistory") ServiceHistoryDto _serviceHistoryDto) {
        this._conditionDto = _conditionDto;
        this._serviceHistoryDto = _serviceHistoryDto;
    }


    public static RequestDto fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);

        try {
            RequestDto requestDto = objectMapper.readValue(json,RequestDto.class);
            return requestDto;
        } catch (IOException e) {
            throw new DvaSopApiDtoError(e);
        }
    }

    public String toJsonString()
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING,true);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiDtoError(e);
        }
    }

}
