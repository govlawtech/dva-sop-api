package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.StandardOfProof;

public class SoPPair {
    private final SoP bopSop;
    private final SoP rhSop;

    public SoPPair(SoP bopSop, SoP rhSop)
    {
        this.bopSop = bopSop;
        assert(bopSop.getStandardOfProof() == StandardOfProof.BalanceOfProbabilities);
        this.rhSop = rhSop;
        assert(rhSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis);
        assert(rhSop.getConditionName().contentEquals(bopSop.getConditionName()));
    }

    public SoP getBopSop() {
        return bopSop;
    }

    public SoP getRhSop() {
        return rhSop;
    }

    public String getConditionName(){
        return bopSop.getConditionName();
    }
}
