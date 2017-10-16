package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.IncidentType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConditionDto {

    @JsonProperty(value = "conditionName")
    private String _conditionName;

    @JsonProperty(value = "incidentType", required = true)
    private final IncidentType _incidentType;

    @JsonProperty("icdCodeVersion")
    private final String _icdCodeVersion;

    @JsonProperty("icdCodeValue")
    private final String _icdCodeValue;

    @JsonProperty("onsetDates")
    private final OnsetDateRangeDto _incidentDateRangeDto;

    @JsonProperty("aggravationDates")
    private final AggravationDateRangeDto _aggravationDateRangeDto;

    @JsonCreator
    public ConditionDto(@JsonProperty("conditionName") String _conditionName,
                        @JsonProperty("incidentType") IncidentType _incidentType,
                        @JsonProperty("icdCodeVersion") String _icdCodeVersion,
                        @JsonProperty("icdCodeValue")  String _icdCodeValue,
                        @JsonProperty("onsetDates") OnsetDateRangeDto _incidentDateRangeDto,
                        @JsonProperty("aggravationDates") AggravationDateRangeDto _aggravationDateRangeDto) {
        this._conditionName = _conditionName;
        this._incidentType = _incidentType;
        this._icdCodeVersion = _icdCodeVersion;
        this._icdCodeValue = _icdCodeValue;
        this._incidentDateRangeDto = _incidentDateRangeDto;
        this._aggravationDateRangeDto = _aggravationDateRangeDto;
    }

    public String get_conditionName() {
        return _conditionName;
    }

    public IncidentType get_incidentType() {
        return _incidentType;
    }

    public String get_icdCodeVersion()
    {
        return _icdCodeVersion;
    }

    public void set_conditionName(String conditionName) {
        _conditionName = conditionName;
    }



    public String get_icdCodeValue() {
        return _icdCodeValue;
    }

    public OnsetDateRangeDto get_incidentDateRangeDto() {
        return _incidentDateRangeDto;
    }

    public AggravationDateRangeDto get_aggravationDateRangeDto() {
        return _aggravationDateRangeDto;
    }



}

