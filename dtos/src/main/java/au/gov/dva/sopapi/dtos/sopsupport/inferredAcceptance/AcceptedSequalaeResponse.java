package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.util.List;

public class AcceptedSequalaeResponse {

    @JsonProperty(value = "recommendations", required = true)
    private final List<String> _recommendations;

    @JsonProperty(value = "sequelae", required = true)
    private final List<AcceptedSequalaeResponseConditionDto> _sequelae;

    @JsonProperty(value = "orderOfApplication", required = true)
    private final List<String> _orderOfApplication;

    public List<AcceptedSequalaeResponseConditionDto> get_sequelae() {
        return _sequelae;
    }

    public List<String> get_orderOfApplication() {
        return _orderOfApplication;
    }


    public AcceptedSequalaeResponse(@JsonProperty("recommendations") List<String> recommendations, @JsonProperty("seqeulae") List<AcceptedSequalaeResponseConditionDto> acceptedSequalaeResponseConditionDtos, @JsonProperty("orderOfApplication") List<String> orderedConditions)
    {
        _sequelae = acceptedSequalaeResponseConditionDtos;
        _orderOfApplication = orderedConditions;
        _recommendations = recommendations;
    }

    public String toJsonString() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        String jsonString = null;
        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
        return jsonString;
    }

}
