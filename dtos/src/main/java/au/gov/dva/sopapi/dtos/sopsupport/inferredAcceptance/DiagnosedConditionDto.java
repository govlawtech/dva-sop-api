package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.time.LocalDate;

public class DiagnosedConditionDto {

    @JsonProperty(value = "conditionName", required = true)
    private final String _name;


    @JsonProperty(value = "onsetDate", required = true)
    private final LocalDate _onsetDate;

    public String get_name() {
        return _name;
    }

    public LocalDate get_onsetDate() {
        return _onsetDate;
    }

    public String get_icdCode() {
        return _icdCode;
    }

    public StandardOfProof get_standardOfProof() {
        return _standardOfProof;
    }

    public LocalDate getOnsetDate() {
        return _onsetDate;
    }

    @JsonProperty(value = "icdCode", required = false)
    private final String _icdCode;

    @JsonProperty(value = "standardOfProof", required = true)
    private final StandardOfProof _standardOfProof;


    @JsonProperty(value = "isOnset", required = true)
    private final boolean _isOnset;

    public boolean get_isOnset() {
        return _isOnset;
    }

    public DiagnosedConditionDto(@JsonProperty("conditionName") String name, @JsonProperty("icdCode") String icdCode, @JsonProperty("onsetDate") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate onsetDate, @JsonProperty("standardOfProof") StandardOfProof acceptedStandardOfProof, @JsonProperty("isOnset") boolean isOnset)
    {
        _name = name;
        _onsetDate = onsetDate;
        _icdCode = icdCode;
        _standardOfProof = acceptedStandardOfProof;
        _isOnset = isOnset;
    }


}
