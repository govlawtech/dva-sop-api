package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.Factor;
import com.google.common.collect.ImmutableList;
import scala.util.Properties;

import java.util.*;

public class SopSupportCaseTrace implements CaseTrace {

    private StringBuilder sb;

    private Optional<Integer> requiredCftsDays = Optional.empty();
    private Optional<Integer> requiredCftsDaysForRh = Optional.empty();
    private Optional<Integer> requiredCftsDaysForBop = Optional.empty();
    private Optional<Integer> actualCftsDays = Optional.empty();
    private Optional<Integer> requiredRhOperationalDays = Optional.empty();
    private Optional<Integer> actualOperationalDays = Optional.empty();
    private Optional<StandardOfProof> applicableStandardOfProof = Optional.empty();
    private Map<ReasoningFor, List<String>> reasonings = new HashMap<>();
    private Optional<String> conditionName = Optional.empty();
    private ImmutableList<Factor> _rhFactors = ImmutableList.of();
    private ImmutableList<Factor> _bopFactors = ImmutableList.of();


    public SopSupportCaseTrace() {

    }

    @Override
    public void setConditionName(String name) {
        conditionName = Optional.of(name);
    }

    @Override
    public Optional<String> getConditionName() {
        return conditionName;
    }

    @Override
    public void addReasoningFor(ReasoningFor type, String msg) {
        if (!reasonings.containsKey(type)) {
            reasonings.put(type, new ArrayList<>());
        }
        reasonings.get(type).add(msg);
        this.addLoggingTrace(msg);
    }

    @Override
    public ImmutableList<String> getReasoningFor(ReasoningFor type) {
        if (reasonings.containsKey(type)) {
            return ImmutableList.copyOf(reasonings.get(type));
        }
        else {
            return ImmutableList.of();
        }
    }

    @Override
    public Map<ReasoningFor, List<String>> getReasonings() {
        return reasonings;
    }

    public void addLoggingTrace(String msg) {
        sb.append(msg + Properties.lineSeparator());
    }

    @Override
    public String getLoggingTraces() {
        return sb.toString();
    }

    @Override
    public void setApplicableStandardOfProof(StandardOfProof standardOfProof) {
        assert !applicableStandardOfProof.isPresent();
        applicableStandardOfProof = Optional.of(standardOfProof);
    }

    @Override
    public Optional<StandardOfProof> getApplicableStandardOfProof() {
        return applicableStandardOfProof;
    }

    @Override
    public void setRequiredCftsDaysForRh(int days) {
        assert !requiredCftsDaysForRh.isPresent();
        requiredCftsDaysForRh = Optional.of(days);
    }

    @Override
    public Optional<Integer> getRequiredCftsDaysForRh() {
        return requiredCftsDaysForRh;
    }

    @Override
    public void setRequiredCftsDaysForBop(int days) {
        assert !requiredCftsDaysForBop.isPresent();
        requiredCftsDaysForBop = Optional.of(days);
    }

    @Override
    public Optional<Integer> getRequiredCftsDaysForBop() {
        return requiredCftsDaysForBop;
    }

    @Override
    public void setRequiredCftsDays(int days) {
        assert !requiredCftsDays.isPresent();
        requiredCftsDays = Optional.of(days);
    }

    @Override
    public Optional<Integer> getRequiredCftsDays() {
        return requiredCftsDays;
    }

    @Override
    public void setActualCftsDays(int days) {
        assert !actualCftsDays.isPresent();
       actualCftsDays = Optional.of(days);
    }

    @Override
    public Optional<Integer> getActualCftsDays() {
        return actualCftsDays;
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

    @Override
    public void setRhFactors(ImmutableList<Factor> rhFactors) {
        _rhFactors = rhFactors;
    }

    @Override
    public ImmutableList<Factor> getRhFactors() {
        return _rhFactors;
    }

    @Override
    public void setBopFactors(ImmutableList<Factor> bopFactors) {
        _bopFactors = bopFactors;
    }

    @Override
    public ImmutableList<Factor> getBopFactors() {
        return _bopFactors;
    }

}
