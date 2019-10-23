package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;

public class AcceptedConditionDto {

    @JsonProperty(value = "conditionName", required = true)
    private final String _name;


    @JsonProperty(value = "onsetDate", required = true)
    private final LocalDate _onsetDate;

    public LocalDate getOnsetDate() {
        return _onsetDate;
    }

    @JsonProperty(value = "icdCode", required = false)
    private final String _icdCode;

    public String get_name() {
        return _name;
    }

    public LocalDate get_onsetDate() {
        return _onsetDate;
    }

    public String get_icdCode() {
        return _icdCode;
    }

    public AcceptedConditionDto(@JsonProperty("conditionName") String name, @JsonProperty("icdCode") String icdCode, @JsonProperty("onsetDate") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate onsetDate)
    {
        _name = name;
        _onsetDate = onsetDate;
        _icdCode = icdCode;
    }


}
