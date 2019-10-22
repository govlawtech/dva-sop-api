package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AcceptedSequalaeResponse {

    @JsonProperty(value = "orderedSequelae", required = true)
    private final List<AcceptedSequalaeResponseConditionDto> _orderedSequelae;

    public List<AcceptedSequalaeResponseConditionDto> getOrderedSequelae() {
        return _orderedSequelae;
    }

    public AcceptedSequalaeResponse(@JsonProperty("orderedSeqeulae") List<AcceptedSequalaeResponseConditionDto> acceptedSequalaeResponseConditionDtos)
    {
        _orderedSequelae = acceptedSequalaeResponseConditionDtos;
    }
}
