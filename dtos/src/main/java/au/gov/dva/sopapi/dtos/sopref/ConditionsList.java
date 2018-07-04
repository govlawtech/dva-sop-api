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

public class ConditionsList {

    @JsonProperty("conditions")
    private final List<ConditionInfo> _conditions;


    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ConditionsList(@JsonProperty("conditions") List<ConditionInfo> conditions)
    {
        _conditions = conditions;
    }


    public static String toJsonString(ConditionsList conditionsList)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        String jsonString = null;
        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(conditionsList);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
        return jsonString;
    }

    public static ConditionsList fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        try {
            ConditionsList conditionsList =
                    objectMapper.readValue(json, ConditionsList.class);
            return conditionsList;
        } catch (IOException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
    }

    @JsonIgnore
    public List<ConditionInfo> get_conditions() {
        return _conditions;
    }


}
