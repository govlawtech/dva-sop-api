package au.gov.dva.sopapi.dtos;

import java.util.Locale;

public enum Rank {
    Officer,
    OtherRank,
    SpecialForces,
    Unknown;

    // used for json (de)seriliasation
    @Override
    public String toString() {
        switch (this)
        {
            case Officer : return "officer";
            case OtherRank: return "other rank";
            case SpecialForces: return "special forces";
            case Unknown: return "unknown";
            default: throw new IllegalArgumentException();
        }

    }

    public static Rank fromString(String rank)
    {
        String lowered = rank.toLowerCase(Locale.ENGLISH);
        if (lowered.contentEquals("officer"))
            return Rank.Officer;
        if (lowered.contentEquals("other rank"))
            return Rank.OtherRank;
        if (lowered.contentEquals("special forces"))
            return Rank.SpecialForces;
        throw new IllegalArgumentException(String.format("Do not recognise rank: %s", rank));
    }

}

