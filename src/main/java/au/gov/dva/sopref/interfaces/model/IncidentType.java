package au.gov.dva.sopref.interfaces.model;

public enum IncidentType {
    Onset("onset"),
    Aggravation("aggravation");

    private final String text;

    @Override
    public String toString() {
        return text;
    }

    IncidentType(String text)
    {
        this.text = text;
    }

    public static IncidentType fromString(String type){
        if (type.contentEquals("onset"))
            return IncidentType.Onset;
        if (type.contentEquals("aggravation"))
            return IncidentType.Aggravation;
        throw new IllegalArgumentException("Unrecognised incident type: " + type);
    }
}
