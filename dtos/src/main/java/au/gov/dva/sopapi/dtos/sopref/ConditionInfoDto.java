package au.gov.dva.sopapi.dtos.sopref;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ConditionInfoDto {

    @JsonProperty("conditionName")
    private final String _conditionName;

    @JsonProperty("rhRegisterId")
    private final String _rhRegisterId;

    @JsonProperty("bopRegisterId")
    private final String _bopRegisterId;

    @JsonProperty("icdCodes")
    private final List<ICDCodeDto> _icdCodes;


    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ConditionInfoDto(@JsonProperty("conditionName") String conditionName,
                            @JsonProperty("rhRegisterId") String rhRegisterId,
                            @JsonProperty("bopRegisterId") String bopRegisterId,
                            @JsonProperty("icdCodes") List<ICDCodeDto> icdCodes)
    {
        _conditionName = conditionName;
        _rhRegisterId = rhRegisterId;
        _bopRegisterId = bopRegisterId;
        _icdCodes = icdCodes;
    }


    public String get_conditionName() {
        return _conditionName;
    }

    public String get_rhRegisterId() {
        return _rhRegisterId;
    }

    public String get_bopRegisterId() {
        return _bopRegisterId;
    }

    public List<ICDCodeDto> get_icdCodes() {
        return _icdCodes;
    }
}
