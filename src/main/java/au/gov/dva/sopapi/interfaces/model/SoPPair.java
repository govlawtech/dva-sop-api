package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.StandardOfProof;

public class SoPPair {
    private final String conditionName;
    private final SoP bopSop;
    private final SoP rhSop;


    public SoPPair(SoP bopSop, SoP rhSop) {
        this.bopSop = bopSop;
        this.rhSop = rhSop;
        assert(bopSop.getConditionName().contentEquals(rhSop.getConditionName()));
        conditionName = bopSop.getConditionName();

    }

    public SoPPair(String conditionName, SoP bopSop, SoP rhSop)
    {
        this.conditionName = conditionName;
        this.bopSop = bopSop;
        assert(bopSop.getStandardOfProof() == StandardOfProof.BalanceOfProbabilities);
        this.rhSop = rhSop;
        assert(rhSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis);
        assert(rhSop.getConditionName().contentEquals(conditionName));
        assert(bopSop.getConditionName().contentEquals(conditionName));
    }

    public SoP getBopSop() {
        return bopSop;
    }

    public SoP getRhSop() {
        return rhSop;
    }

    public String getConditionName(){
        return conditionName;
    }
}
