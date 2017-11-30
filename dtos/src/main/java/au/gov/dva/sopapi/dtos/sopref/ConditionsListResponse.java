package au.gov.dva.sopapi.dtos.sopref;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.util.List;

public class ConditionsListResponse {

    @JsonProperty("conditions")
    private final List<ConditionInfoDto> _conditions;


    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ConditionsListResponse(@JsonProperty("conditions") List<ConditionInfoDto> conditions)
    {
        _conditions = conditions;
    }


    public static String toJsonString(ConditionsListResponse conditionsListResponse)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        String jsonString = null;
        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(conditionsListResponse);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
        return jsonString;
    }

    public static ConditionsListResponse fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        try {
            ConditionsListResponse conditionsListResponse =
                    objectMapper.readValue(json, ConditionsListResponse.class);
            return conditionsListResponse;
        } catch (IOException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
    }

    @JsonIgnore
    public List<ConditionInfoDto> get_conditions() {
        return _conditions;
    }


}
