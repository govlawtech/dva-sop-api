package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.ICDCodeDto;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;

public class DiagnosedConditionDto {

    @JsonProperty(value = "conditionName", required = true)
    private final String _name;

    @JsonProperty(value = "date", required = true)
    private final LocalDate _date;

    public String get_name() {
        return _name;
    }

    public LocalDate get_date() {
        return _date;
    }

    public ICDCodeDto get_icdCode() {
        return _icdCode;
    }

    public StandardOfProof get_standardOfProof() {
        return _standardOfProof;
    }


    @JsonProperty(value = "icdCode", required = false)
    private final ICDCodeDto _icdCode;

    @JsonProperty(value = "standardOfProof", required = true)
    private final StandardOfProof _standardOfProof;

    @JsonProperty(value = "isOnset", required = true)
    private final boolean _isOnset;

    @JsonProperty(value = "side",required = false)
    private final Side _side;

    public Side get_side() {return _side;}

    public boolean get_isOnset() {
        return _isOnset;
    }

    public DiagnosedConditionDto(@JsonProperty("conditionName") String name, @JsonProperty("icdCode") ICDCodeDto icdCode, @JsonProperty("side") Side side, @JsonProperty("date") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate date, @JsonProperty("standardOfProof") StandardOfProof acceptedStandardOfProof, @JsonProperty("isOnset") boolean isOnset)
    {
        _name = name;
        _date = date;
        _icdCode = icdCode;
        _standardOfProof = acceptedStandardOfProof;
        _isOnset = isOnset;
        _side = side;
    }


}
