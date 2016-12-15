package au.gov.dva.interfaces.model;

public enum ServiceBranch {

    ARMY,
    RAN,
<<<<<<< HEAD
    RAAF

=======
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
>>>>>>> sopsupport
}
