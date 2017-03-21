package au.gov.dva.sopapi.dtos;

import java.util.Locale;

public enum ServiceBranch {

    ARMY,
    RAN,
    RAAF;

    // used for json (de)serialization
    @Override
    public String toString() {
        switch (this)
        {
            case ARMY: return "Australian Army";
            case RAN: return "Royal Australian Navy";
            case RAAF: return "Royal Australian Air Force";
            default: throw new IllegalArgumentException();
        }
    }

    public static ServiceBranch fromString(String value)
    {
        String lowered = value.toLowerCase(Locale.ENGLISH);
        if (lowered.contentEquals("australian army"))
            return ServiceBranch.ARMY;
        if (lowered.contentEquals("royal australian navy"))
            return ServiceBranch.RAN;
        if (lowered.contentEquals("royal australian air force"))
            return ServiceBranch.RAAF;
        throw new IllegalArgumentException(String.format("Unrecognised service branch: %s", value));
    }
}
