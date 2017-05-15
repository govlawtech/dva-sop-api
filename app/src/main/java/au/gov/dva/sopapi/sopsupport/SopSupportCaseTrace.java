package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.exceptions.DvaSopApiError;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import scala.Int;
import scala.util.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SopSupportCaseTrace implements CaseTrace {

    private StringBuilder sb;

    private Optional<Integer> rhRequiredCftsDays = Optional.empty();
    private Optional<Integer> boPRequiredCftsDays = Optional.empty();
    private Optional<Integer> requiredRhOperationalDays = Optional.empty();
    private Optional<Integer> actualOperationalDays = Optional.empty();
    private Optional<Integer> actualCftsDays = Optional.empty();

    public SopSupportCaseTrace(String caseId) {
        sb = new StringBuilder(String.format("Case ID: %s%n", caseId));
    }

    public void addLoggingTrace(String msg) {
        sb.append(msg + Properties.lineSeparator());
    }

    @Override
    public String getLoggingTraces() {
        return sb.toString();
    }

    @Override
    public void setRequiredCftsDays(StandardOfProof standardOfProof, int days) {
        switch (standardOfProof) {
            case ReasonableHypothesis: {
                assert !rhRequiredCftsDays.isPresent();
                rhRequiredCftsDays = Optional.of(days);
                break;
            }
            case BalanceOfProbabilities: {
                assert !boPRequiredCftsDays.isPresent();
                boPRequiredCftsDays = Optional.of(days);
                break;
            }
            default: throw new DvaSopApiError("Unrecognised standard of proof: " + standardOfProof);
        }
    }

    @Override
    public Optional<Integer> getRequiredCftsDays(StandardOfProof standardOfProof) {
        switch (standardOfProof)
        {
            case ReasonableHypothesis: return rhRequiredCftsDays;
            case BalanceOfProbabilities: return boPRequiredCftsDays;
            default: throw new DvaSopApiError("Unrecognised standard of proof: " + standardOfProof);
        }
    }

    @Override
    public void setActualCftsDays(int days) {
        assert !actualCftsDays.isPresent();
       actualCftsDays = Optional.of(days);
    }

    @Override
    public void setRequiredOperationalDaysForRh(int days) {
        assert !requiredRhOperationalDays.isPresent();
        requiredRhOperationalDays = Optional.of(days);
    }

    @Override
    public Optional<Integer> getRequiredOperationalDaysForRh() {
        return requiredRhOperationalDays;
    }

    @Override
    public void setActualOperationalDays(int days) {
        assert !actualOperationalDays.isPresent();
        actualOperationalDays = Optional.of(days);
    }

    @Override
    public Optional<Integer> getActualOperationalDays() {
        return actualOperationalDays;
    }
}
