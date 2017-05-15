package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Optional;

public interface CaseTrace {

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

}
