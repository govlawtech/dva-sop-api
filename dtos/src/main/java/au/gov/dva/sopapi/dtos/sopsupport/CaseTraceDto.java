package au.gov.dva.sopapi.dtos.sopsupport;

import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.FactorDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CaseTraceDto {

    @JsonProperty("standardOfProof")
    private Optional<StandardOfProof> _standardOfProof;

    @JsonProperty("requiredDaysOfCfts")
    private Optional<Integer> _requiredCftsDays;

    @JsonProperty("requiredDaysOfCftsForRh")
    private Optional<Integer> _requiredCftsDaysForRh;

    @JsonProperty("requiredDaysOfCftsForBop")
    private Optional<Integer> _requiredCftsDaysForBop;

    @JsonProperty("actualDaysOfCfts")
    private Optional<Integer> _actualCftsDays;

    @JsonProperty("requiredDaysOfOperationalServiceForRhStandard")
    private Optional<Integer> _requiredOperationalDaysForRh;

    @JsonProperty("actualDaysOfOperationalServiceInTestPeriod")
    private Optional<Integer> _actualOperationalDays;

    @JsonProperty("rhFactors")
    private List<FactorDto> _rhFactors;

    @JsonProperty("bopFactors")
    private List<FactorDto> _bopFactors;

    @JsonProperty("reasonings")
    private Map<ReasoningFor, List<String>> _reasonings;

    @JsonProperty("logTrace")
    private String _logTrace;

    public CaseTraceDto(){}

    public CaseTraceDto(Optional<StandardOfProof> standardOfProof,
                        Optional<Integer> requiredCftsDays,
                        Optional<Integer> requiredCftsDaysForRh,
                        Optional<Integer> requiredCftsDaysForBop,
                        Optional<Integer> actualCftsDays,
                        Optional<Integer> requiredOperationalDaysForRh,
                        Optional<Integer> actualOperationalDays,
                        List<FactorDto> rhFactors,
                        List<FactorDto> bopFactors,
                        Map<ReasoningFor, List<String>> reasonings,
                        String logTrace)
    {
        _standardOfProof = standardOfProof;
        _requiredCftsDays = requiredCftsDays;
        _requiredCftsDaysForRh = requiredCftsDaysForRh;
        _requiredCftsDaysForBop = requiredCftsDaysForBop;
        _actualCftsDays = actualCftsDays;
        _requiredOperationalDaysForRh = requiredOperationalDaysForRh;
        _actualOperationalDays = actualOperationalDays;
        _rhFactors = ImmutableList.copyOf(rhFactors);
        _bopFactors = ImmutableList.copyOf(bopFactors);
        _reasonings = ImmutableMap.copyOf(reasonings);
        _logTrace = logTrace;
    }

    @JsonIgnore
    public String getLogTrace() {
        return _logTrace;
    }

    @JsonIgnore
    public Optional<StandardOfProof> getStandardOfProof() {
        return _standardOfProof;
    }

    @JsonIgnore
    public Optional<Integer> getRequiredCftsDays() {
        return _requiredCftsDays;
    }

    @JsonIgnore
    public Optional<Integer> getRequiredCftsDaysForRh() {
        return _requiredCftsDaysForRh;
    }

    @JsonIgnore
    public Optional<Integer> getRequiredCftsDaysForBop() {
        return _requiredCftsDaysForBop;
    }

    @JsonIgnore
    public Optional<Integer> getActualCftsDays() {
        return _actualCftsDays;
    }

    @JsonIgnore
    public Optional<Integer> getRequiredOperationalDaysForRh() {
        return _requiredOperationalDaysForRh;
    }

    @JsonIgnore
    public Optional<Integer> getActualOperationalDays() {
        return _actualOperationalDays;
    }

    @JsonIgnore
    public List<FactorDto> getRhFactors() { return _rhFactors; }

    @JsonIgnore
    public List<FactorDto> getBopFactors() { return _bopFactors; }

    @JsonIgnore
    public Map<ReasoningFor, List<String>> getReasonings() { return _reasonings; }
}
