package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.model.Factor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.Set;

public interface CaseTrace {

    enum ReasoningFor { STANDARD_OF_PROOF, ABORT_PROCESSING, MEETING_FACTORS }

    void addReasoningFor(ReasoningFor type, String msg);
    ImmutableList<String> getReasoningFor(ReasoningFor type);

    void addLoggingTrace(String msg);
    String getLoggingTraces();

    void setApplicableStandardOfProof(StandardOfProof standardOfProof);
    StandardOfProof getApplicableStandardOfProof();

    void setRequiredCftsDays(int days);
    Optional<Integer> getRequiredCftsDays();

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
