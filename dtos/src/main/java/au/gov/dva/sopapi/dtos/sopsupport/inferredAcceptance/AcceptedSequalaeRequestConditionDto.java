package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.time.LocalDate;

public class AcceptedSequalaeRequestConditionDto {

    @JsonProperty(value = "conditionName", required = true)
    private final String _name;

    public String getName() {
        return _name;
    }

    @JsonProperty(value = "onsetDate", required = true)
    private final LocalDate _onsetDate;

    public LocalDate getOnsetDate() {
        return _onsetDate;
    }

    @JsonProperty(value = "icdCode", required = false)
    private final String _icdCode;

    @JsonProperty(value = "accepted", required = true)
    private final boolean _accepted;

    public boolean isAccepted() {
        return _accepted;
    }

    public AcceptedSequalaeRequestConditionDto(@JsonProperty("conditionName") String name, @JsonProperty("icdCode") String icdCode, @JsonProperty("onsetDate") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate onsetDate, @JsonProperty("isAccepted") boolean accepted)
    {
        _name = name;
        _onsetDate = onsetDate;
        _icdCode = icdCode;
        _accepted = accepted;
    }


}
