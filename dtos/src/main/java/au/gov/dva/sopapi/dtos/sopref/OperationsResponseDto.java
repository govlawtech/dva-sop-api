package au.gov.dva.sopapi.dtos.sopref;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoError;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.util.List;

public class OperationsResponseDto {

    @JsonProperty("registerIds")
    private final List<String> _registerIds;

    @JsonProperty("operations")
    private final List<OperationDto> _operations;

    public OperationsResponseDto(@JsonProperty("registerIds") List<String> _registerIds, @JsonProperty("operations") List<OperationDto> _operations) {
        this._registerIds = _registerIds;
        this._operations = _operations;
    }

    public static String toJsonString(OperationsResponseDto operationsResponseDto)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        String jsonString = null;
        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(operationsResponseDto);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiDtoError(e);
        }
        return jsonString;
    }

    public static OperationsResponseDto fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        try {
            OperationsResponseDto operationsResponseDto =
                    objectMapper.readValue(json, OperationsResponseDto.class);
            return operationsResponseDto;
        } catch (IOException e) {
            throw new DvaSopApiDtoError(e);
        }
    }

}
