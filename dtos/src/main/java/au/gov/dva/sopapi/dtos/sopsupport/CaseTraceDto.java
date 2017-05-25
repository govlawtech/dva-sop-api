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
    private final StandardOfProof _standardOfProof;

    @JsonProperty("requiredDaysOfCfts")
    private final Optional<Integer> _requiredCftsDays;

    @JsonProperty("requiredDaysOfCftsForRh")
    private final Optional<Integer> _requiredCftsDaysForRh;

    @JsonProperty("requiredDaysOfCftsForBop")
    private final Optional<Integer> _requiredCftsDaysForBop;

    @JsonProperty("actualDaysOfCfts")
    private final Optional<Integer> _actualCftsDays;

    @JsonProperty("requiredDaysOfOperationalServiceForRhStandard")
    private final Optional<Integer> _requiredOperationalDaysForRh;

    @JsonProperty("actualDaysOfOperationalServiceInTestPeriod")
    private final Optional<Integer> _actualOperationalDays;

    @JsonProperty("rhFactors")
    private final ImmutableList<FactorDto> _rhFactors;

    @JsonProperty("bopFactors")
    private final ImmutableList<FactorDto> _bopFactors;

    @JsonProperty("reasonings")
    private final ImmutableMap<ReasoningFor, List<String>> _reasonings;

    private String logTrace;

    public CaseTraceDto(StandardOfProof standardOfProof,
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
        this.logTrace = logTrace;
    }

    @JsonIgnore
    public String getLogTrace() {
        return logTrace;
    }

    @JsonIgnore
    public StandardOfProof getStandardOfProof() {
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
    public ImmutableList<FactorDto> getRhFactors() { return _rhFactors; }

    @JsonIgnore
    public ImmutableList<FactorDto> getBopFactors() { return _bopFactors; }

    @JsonIgnore
    public ImmutableMap<ReasoningFor, List<String>> getReasonings() { return _reasonings; }
}
