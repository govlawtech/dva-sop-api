package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.util.List;

public class SoPPair {
    private final String conditionName;
    private final SoP bopSop;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoPPair soPPair = (SoPPair) o;

        if (!conditionName.equals(soPPair.conditionName)) return false;
        if (!bopSop.equals(soPPair.bopSop)) return false;
        return rhSop.equals(soPPair.rhSop);
    }

    @Override
    public int hashCode() {
        int result = conditionName.hashCode();
        result = 31 * result + bopSop.hashCode();
        result = 31 * result + rhSop.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return conditionName;
    }

    private final SoP rhSop;

    public SoPPair(SoP bopSop, SoP rhSop) {
        this.bopSop = bopSop;
        this.rhSop = rhSop;
        assert (bopSop.getConditionName().contentEquals(rhSop.getConditionName()));
        conditionName = bopSop.getConditionName();
    }

    public SoPPair(String conditionName, SoP bopSop, SoP rhSop) {
        this.conditionName = conditionName;
        this.bopSop = bopSop;
        assert (bopSop.getStandardOfProof() == StandardOfProof.BalanceOfProbabilities);
        this.rhSop = rhSop;
        assert (rhSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis);
        assert (rhSop.getConditionName().contentEquals(conditionName));
        assert (bopSop.getConditionName().contentEquals(conditionName));
    }

    public SoP getBopSop() {
        return bopSop;
    }

    public SoP getRhSop() {
        return rhSop;
    }

    public String getConditionName() {

        return conditionName;
    }

    public LocalDate getEffectiveFromDate() {
        assert (bopSop.getEffectiveFromDate().equals(rhSop.getEffectiveFromDate()));
        return bopSop.getEffectiveFromDate();
    }

    public ImmutableSet<ICDCode> getICDCodes() {
        return (new ImmutableSet.Builder<ICDCode>()
                .addAll(bopSop.getICDCodes())
                .addAll(rhSop.getICDCodes()))
                .build();
    }

}
