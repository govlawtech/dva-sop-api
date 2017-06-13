package au.gov.dva.sopapi.dtos.sopref;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.util.List;

public class SoPReferenceResponse {

    @JsonProperty("applicableFactors")
    private final List<SoPFactorsResponse> _sops;

    public SoPReferenceResponse(@JsonProperty("applicableFactors") List<SoPFactorsResponse> soPFactorsResponses)
    {
        _sops = soPFactorsResponses;
    }

    public static String toJsonString(SoPReferenceResponse soPReferenceResponse)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        String jsonString = null;

        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(soPReferenceResponse);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }

        return jsonString;
    }

    public static SoPReferenceResponse fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        try {
            SoPReferenceResponse soPReferenceResponse = objectMapper.readValue(json, SoPReferenceResponse.class);
            return soPReferenceResponse;
        } catch (IOException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }
    }

    @JsonIgnore
    public List<SoPFactorsResponse> getSoPFactorsResponses() {
        return _sops;
    }
}
