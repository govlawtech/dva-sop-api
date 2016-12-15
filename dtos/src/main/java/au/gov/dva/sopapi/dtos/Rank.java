package au.gov.dva.sopapi.dtos;

public enum Rank {
    Officer,
    OtherRank,
    SpecialForces;

    // used for json (de)seriliasation
    @Override
    public String toString() {
        switch (this)
        {
            case Officer : return "officer";
            case OtherRank: return "other rank";
            case SpecialForces: return "special forces";
            default: throw new IllegalArgumentException();
        }

    }
}

