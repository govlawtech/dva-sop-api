package au.gov.dva.sopref.interfaces.model;

public enum ServiceType {
    WARLIKE("WARLIKE"),
    NON_WARLIKE("non-WARLIKE");

    private String text;
    ServiceType(String text)
    {
        this.text = text;
    }

    public String getText() { return text;};
}
