package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Optional;

public interface CaseTrace {

    void addLoggingTrace(String msg);
    String getLoggingTraces();

    void setRequiredCftsDays(StandardOfProof standardOfProof, int days);
    Optional<Integer> getRequiredCftsDays(StandardOfProof standardOfProof);

    void setActualCftsDays(int days);
    Optional<Integer> getActualCftsDays();

    void setRequiredOperationalDaysForRh(int days);
    Optional<Integer> getRequiredOperationalDaysForRh();

    void setActualOperationalDays(int days);
    Optional<Integer> getActualOperationalDays();

}
