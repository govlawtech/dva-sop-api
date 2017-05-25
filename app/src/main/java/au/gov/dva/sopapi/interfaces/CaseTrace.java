package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.model.Factor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CaseTrace {

    void addReasoningFor(ReasoningFor type, String msg);
    ImmutableList<String> getReasoningFor(ReasoningFor type);
    Map<ReasoningFor, List<String>> getReasonings();

    void addLoggingTrace(String msg);
    String getLoggingTraces();

    void setApplicableStandardOfProof(StandardOfProof standardOfProof);
    StandardOfProof getApplicableStandardOfProof();

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
}
