package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.sopref.FactorDto;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AcceptedSequalaeResponseConditionDto {

    @JsonProperty(value = "condition", required = true)
    private final String _conditionName;

    @JsonProperty(value = "instrumentIdForReasonableHypothesis")
    private final String rhFrlId;

    @JsonProperty(value = "instrumentIdForBalanceOfProbabilities")
    private final String bopFrlId;

    @JsonProperty(value = "factorsForReasonableHypothesis")
    private final List<FactorDto> _rhFactors;

    @JsonProperty(value = "factorsForBalanceOfProbabilities")
    private final List<FactorDto> _bopFactors;

    public AcceptedSequalaeResponseConditionDto(@JsonProperty("condition") String conditionName, @JsonProperty("instrumentIdForReasonableHypothesis") String rhInstrumentId, @JsonProperty("instrumentIdForReasonableHypothesis") String instrumentIdForBoP, @JsonProperty("factorsForReasonableHypothesis") List<FactorDto> rhFactors, @JsonProperty("factorsForBalanceOfProbabilities") List<FactorDto> bopFactors) {
        _conditionName = conditionName;
        rhFrlId = rhInstrumentId;
        bopFrlId = instrumentIdForBoP;
        _rhFactors = rhFactors;
        _bopFactors = bopFactors;
    }

}
