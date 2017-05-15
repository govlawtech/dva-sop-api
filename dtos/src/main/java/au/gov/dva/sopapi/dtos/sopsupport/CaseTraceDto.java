package au.gov.dva.sopapi.dtos.sopsupport;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class CaseTraceDto {

    @JsonProperty("requiredContinuousServiceDays")
    private final Optional<Integer> _requiredCftsDays;

    @JsonProperty("actualDaysOfContinuousService")
    private final Optional<Integer> _actualCftsDays;

    @JsonProperty("requiredOperationalDaysForRh")
    private final Optional<Integer> _requiredOperationalDaysForRh;

    @JsonProperty("actualOperationalDays")
    private final Optional<Integer> _actualOperationalDays;

    private String logTrace;

    public CaseTraceDto(Optional<Integer> requiredCftsDays,
                        Optional<Integer> actualCftsDays,
                        Optional<Integer> requiredOperationalDaysForRh,
                        Optional<Integer> actualOperationalDays,
                        String logTrace)
    {
        _requiredCftsDays = requiredCftsDays;
        _actualCftsDays = actualCftsDays;
        _requiredOperationalDaysForRh = requiredOperationalDaysForRh;
        _actualOperationalDays = actualOperationalDays;
        this.logTrace = logTrace;
    }



    @JsonIgnore
    public String getLogTrace() {
        return logTrace;
    }

    @JsonIgnore
    public Optional<Integer> getRequiredCftsDays() {
        return _requiredCftsDays;
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
}
