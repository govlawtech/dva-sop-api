package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SequelaeRequestDto {

    @JsonProperty(value = "acceptedConditions", required = true)
    private final List<AcceptedConditionDto> _acceptedConditions;

    @JsonProperty(value = "diagnosedConditions", required = true)
    private final List<DiagnosedConditionDto> _diagnosedConditions;



    public List<AcceptedConditionDto> get_acceptedConditions() {
        return _acceptedConditions;
    }

    public List<DiagnosedConditionDto> get_diagnosedConditions() {
        return _diagnosedConditions;
    }

    public SequelaeRequestDto(@JsonProperty("acceptedConditions") List<AcceptedConditionDto> acceptedConditions, @JsonProperty("diagnosedConditions") List<DiagnosedConditionDto> diagnosedConditions)
    {
        _acceptedConditions = acceptedConditions;
        _diagnosedConditions = diagnosedConditions;
    }

    public static SequelaeRequestDto fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            SequelaeRequestDto acceptedSequalaeRequestConditionDto = objectMapper.readValue(json,SequelaeRequestDto.class);
            return acceptedSequalaeRequestConditionDto;
        } catch (IOException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
    }


    public ImmutableList<String> getValidationErrors(ImmutableSet<String> currentConditionNames) {
        List<String> acceptedConditionNames = _acceptedConditions.stream().map(c -> c.get_name()).collect(Collectors.toList());
        List<String> diagnosedConditionNames = _diagnosedConditions.stream().map(c -> c.get_name()).collect(Collectors.toList());
        ImmutableSet<String> allConditionNames = ImmutableSet.<String>builder().addAll(acceptedConditionNames).addAll(diagnosedConditionNames).build();
        List<String> invalidConditionNames = allConditionNames.stream().filter(n -> !currentConditionNames.contains(n)).collect(Collectors.toList());
        if (invalidConditionNames.isEmpty())
        {
            return ImmutableList.of();
        }
        else {
            return ImmutableList.of(String.format("The following are not current SoP conditions (check spelling and punctuation): %s", String.join(";",invalidConditionNames)));
        }
    }
}
