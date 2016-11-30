package au.gov.dva.sopref.interfaces.model;

public enum ServiceType {
    WARLIKE("warlike"),
    NON_WARLIKE("non-warlike");


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
