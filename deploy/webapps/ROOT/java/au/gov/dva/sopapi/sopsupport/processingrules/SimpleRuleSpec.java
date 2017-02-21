package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.Rank;

public class SimpleRuleSpec {
    private final RankSpec officer;
    private final RankSpec other;
    private final RankSpec specialForces;

    public SimpleRuleSpec(RankSpec officer, RankSpec other, RankSpec specialForces)
    {

        this.officer = officer;
        this.other = other;
        this.specialForces = specialForces;
    }

    public RankSpec getSpec(Rank rank)
    {
        switch (rank)
        {
            case Officer: return officer;
            case OtherRank: return other;
            case SpecialForces: return specialForces;
            default: throw new IllegalArgumentException();
        }
    }

}


