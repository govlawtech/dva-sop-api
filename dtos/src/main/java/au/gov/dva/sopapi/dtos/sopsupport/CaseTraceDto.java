package au.gov.dva.sopapi.dtos.sopsupport;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class CaseTraceDto {

    @JsonProperty("requiredContinuousServiceDaysForRh")
    private final int requiredCftsDaysForRh;

    @JsonProperty("requiredContinuousServiceDaysForBoP")
    private final int requiredCftsDaysForBoP;

    @JsonProperty("actualDaysOfContinuousService")
    private final int actualCftsDays;

    @JsonProperty("requiredOperationalDaysForRh")
    private final int requiredOperationalDaysForRh;

    @JsonProperty("actualOperationalDays")
    private final int actualOperationalDays;

    private String logTrace;

    public CaseTraceDto(int requiredCftsDaysForRh,
                        int requiredCftsDaysForBoP,
                        int actualCftsDays,
                        int requiredOperationalDaysForRh,
                        int actualOperationalDays,
                        String logTrace)
    {
        this.requiredCftsDaysForRh = requiredCftsDaysForRh;
        this.requiredCftsDaysForBoP = requiredCftsDaysForBoP;
        this.actualCftsDays = actualCftsDays;
        this.requiredOperationalDaysForRh = requiredOperationalDaysForRh;
        this.actualOperationalDays = actualOperationalDays;
        this.logTrace = logTrace;
    }

    @JsonIgnore
    public int getRequiredCftsDaysForRh() {
        return requiredCftsDaysForRh;
    }

    @JsonIgnore
    public int getRequiredCftsDaysForBoP() {
        return requiredCftsDaysForBoP;
    }

    @JsonIgnore
    public int getActualCftsDays() {
        return actualCftsDays;
    }

    @JsonIgnore
    public int getRequiredOperationalDaysForRh() {
        return requiredOperationalDaysForRh;
    }

    @JsonIgnore
    public int getActualOperationalDays() {
        return actualOperationalDays;
    }

    @JsonIgnore
    public String getLogTrace() {
        return logTrace;
    }
}
