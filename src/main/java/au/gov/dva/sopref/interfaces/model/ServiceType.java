package au.gov.dva.sopref.interfaces.model;

public enum ServiceType {
    WARLIKE("warlike"),
    NON_WARLIKE("non-warlike");

    public static ServiceType fromText(String text)
    {
        if (text.contentEquals("warlike"))
            return ServiceType.WARLIKE;
        if (text.contentEquals("non-warlike"))
            return ServiceType.NON_WARLIKE;
        else throw new IllegalArgumentException(String.format("Unrecognised service type: %s", text));

    }

    @Override
    public String toString() {
        return text;
    }

    private String text;
    ServiceType(String text)
    {
        this.text = text;
    }

}
