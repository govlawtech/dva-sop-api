package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import au.gov.dva.sopapi.dtos.sopref.FactorDto;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.util.List;


public class AcceptedSequalaeResponseConditionDto {

    @JsonProperty(value = "conditionName", required = true)
    private final String _conditionName;

    @JsonProperty(value = "reasonableHypothesis", required = true)
    private final FactorListDto _rhFactors;

    @JsonProperty(value = "balanceOfProbabilities", required = true)
    private final FactorListDto _bopFactors;

    public AcceptedSequalaeResponseConditionDto(@JsonProperty("conditionName") String conditionName, @JsonProperty("reasonableHypothesis") FactorListDto rhFactors, @JsonProperty("balanceOfProbabilities") FactorListDto bopFactors)
    {
        _conditionName = conditionName;
        _rhFactors = rhFactors;
        _bopFactors = bopFactors;
    }



}
