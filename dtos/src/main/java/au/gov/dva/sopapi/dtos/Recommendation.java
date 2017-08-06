package au.gov.dva.sopapi.dtos;




public enum Recommendation {
    APPROVED,
    STP_NOT_APPLICABLE,
    CHECK_RH_BOP_MET,
    CHECK_RH;

    // Don't change these without also changing the corresponding yaml.
    private final static String RECOMMEDATION_TEXT_APPROVED = "Accept claim";
    private final static String RECOMMEDATION_TEXT_STP_NOT_APPLICABLE = "Review claim details";
    private final static String RECOMMEDATION_TEXT_CHECK_RH_BOP_MET = "Review operational service to check all factors, otherwise accept as BoP factor met";
    private final static String RECOMMEDATION_TEXT_CHECK_RH = "Review operational service to check all factors";

    // used for json (de)serialization
    @Override
    public String toString() {
        switch (this)
        {
            case APPROVED: return RECOMMEDATION_TEXT_APPROVED;
            case STP_NOT_APPLICABLE: return RECOMMEDATION_TEXT_STP_NOT_APPLICABLE;
            case CHECK_RH_BOP_MET: return RECOMMEDATION_TEXT_CHECK_RH_BOP_MET;
            case CHECK_RH: return RECOMMEDATION_TEXT_CHECK_RH;
            default: throw new IllegalArgumentException();
        }
    }

    public static Recommendation fromString(String value)
    {
        if (value.contentEquals(RECOMMEDATION_TEXT_APPROVED))
            return APPROVED;
        if (value.contentEquals(RECOMMEDATION_TEXT_STP_NOT_APPLICABLE))
            return STP_NOT_APPLICABLE;
        if (value.contentEquals(RECOMMEDATION_TEXT_CHECK_RH_BOP_MET))
            return CHECK_RH_BOP_MET;
        if (value.contentEquals(RECOMMEDATION_TEXT_CHECK_RH))
            return CHECK_RH;
        throw new IllegalArgumentException(String.format("Unrecognised recommendation: %s", value));
    }
}
