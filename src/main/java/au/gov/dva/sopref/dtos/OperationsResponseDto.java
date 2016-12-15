package au.gov.dva.sopref.dtos;

<<<<<<< HEAD
import au.gov.dva.sopref.exceptions.DvaSopApiError;
=======
import au.gov.dva.exceptions.DvaSopApiError;
>>>>>>> sopsupport
import au.gov.dva.interfaces.model.ServiceDetermination;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.stream.Collectors;

public class OperationsResponseDto {

    @JsonProperty("registerIds")
    private final List<String> _registerIds;

    @JsonProperty("operations")
    private final List<OperationDto> _operations;

    public OperationsResponseDto(List<String> _registerIds, List<OperationDto> _operations) {
        this._registerIds = _registerIds;
        this._operations = _operations;
    }

    public static OperationsResponseDto build(ImmutableSet<ServiceDetermination> latestServiceDeterminations)
    {

        List<String> registerIds = latestServiceDeterminations.stream()
                .map(sd -> sd.getRegisterId())
                .collect(Collectors.toList());

        List<OperationDto> operationDtos = latestServiceDeterminations.stream()
                .flatMap(sd -> sd.getOperations().stream())
                .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
                .map(OperationDto::fromOperation)
                .collect(Collectors.toList());

        return new OperationsResponseDto(registerIds,operationDtos);
    }


    public static String toJsonString(OperationsResponseDto operationsResponseDto)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        String jsonString = null;
        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(operationsResponseDto);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiError(e);
        }
        return jsonString;
    }

}
