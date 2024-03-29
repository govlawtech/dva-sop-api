package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.JustifiedMilitaryActivity;
import au.gov.dva.sopapi.interfaces.model.MilitaryActivity;
import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CaseTrace {

    void setConditionName(String name);
    Optional<String> getConditionName();

    void addReasoningFor(ReasoningFor type, String msg);
    ImmutableList<String> getReasoningFor(ReasoningFor type);
    Map<ReasoningFor, List<String>> getReasonings();

    void addLoggingTrace(String msg);
    String getLoggingTraces();

    void setApplicableStandardOfProof(StandardOfProof standardOfProof);
    Optional<StandardOfProof> getApplicableStandardOfProof();

    void setRequiredCftsDays(int days);
    Optional<Integer> getRequiredCftsDays();

    void setRequiredCftsDaysForRh(int days);
    Optional<Integer> getRequiredCftsDaysForRh();

    void setRequiredCftsDaysForBop(int days);
    Optional<Integer> getRequiredCftsDaysForBop();

    void setActualCftsDays(int days);
    Optional<Integer> getActualCftsDays();

    void setRequiredOperationalDaysForRh(int days);
    Optional<Integer> getRequiredOperationalDaysForRh();

    void setActualOperationalDays(int days);
    Optional<Integer> getActualOperationalDays();

    void setRhFactors(ImmutableList<Factor> rhFactors);
    ImmutableList<Factor> getRhFactors();

    void setBopFactors(ImmutableList<Factor> bopFactors);
    ImmutableList<Factor> getBopFactors();

    void setTestInterval(Interval testInterval);
    Interval getTestInterval();

    ImmutableList<JustifiedMilitaryActivity> getRelevantOperations();
    void SetRelevantOperations(ImmutableList<JustifiedMilitaryActivity> justifiedMilitaryActivities);

    default boolean isComplete() {
        if (!getReasonings().containsKey(ReasoningFor.ABORT_PROCESSING))
        {
            return  getConditionName().isPresent() &&
                    getApplicableStandardOfProof().isPresent() &&
                    getRequiredCftsDays().isPresent() &&
                    getRequiredCftsDaysForRh().isPresent() &&
                     getRequiredCftsDaysForBop().isPresent() &&
                    getActualCftsDays().isPresent() &&
                    getRequiredOperationalDaysForRh().isPresent() &&
                    getActualOperationalDays().isPresent() &&
                     !getRhFactors().isEmpty();
            // bop factors can be empty

        }
        return true;
    }
}
