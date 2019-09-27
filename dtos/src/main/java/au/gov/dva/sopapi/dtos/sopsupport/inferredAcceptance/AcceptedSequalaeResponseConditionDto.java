package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.sopref.FactorDto;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AcceptedSequalaeResponseConditionDto {

    @JsonProperty(value = "conditions", required = true)
    private final String _conditionName;

    @JsonProperty(value = "instrumentIdForReasonableHypothesis")
    private final String rhFrlId;

    @JsonProperty(value = "instrumentIdForBalanceOfProbabilities")
    private final String bopFrlId;

    @JsonProperty(value = "")
    private final List<FactorDto> rhFactors;

    private final List<FactorDto> bopFactors;

    public AcceptedSequalaeResponseConditionDto(@JsonProperty(""))

}
