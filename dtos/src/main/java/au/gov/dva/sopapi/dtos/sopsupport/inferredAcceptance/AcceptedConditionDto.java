package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.sopref.ICDCodeDto;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;


public class AcceptedConditionDto {

    @JsonProperty(value = "conditionName", required = true)
    private final String _name;


    @JsonProperty(value = "date", required = true)
    private final LocalDate _date;

    public LocalDate getDate() {
        return _date;
    }

    @JsonProperty(value = "icdCode", required = false)
    private final ICDCodeDto _icdCode;

    @JsonProperty(value = "side",required = false)
    private final Side _side;

    public String get_name() {
        return _name;
    }


    public ICDCodeDto get_icdCode() {
        return _icdCode;
    }

    public Side get_side() {return _side;}

    public AcceptedConditionDto(@JsonProperty("conditionName") String name, @JsonProperty("icdCode") ICDCodeDto icdCode, @JsonProperty("side") Side side, @JsonProperty("onsetDate") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate date)
    {
        _name = name;
        _date = date;
        _icdCode = icdCode;
        _side = side;
    }


}
