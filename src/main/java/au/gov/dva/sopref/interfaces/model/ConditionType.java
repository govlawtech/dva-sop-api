package au.gov.dva.sopref.interfaces.model;

public enum ConditionType {
    ACUTE("acute"),ACCUMULATED("accumulated over time");
    @Override
    public String toString() {
        return text;
    }

    private String text;
    ConditionType(String text)
    {
        this.text = text;
    }
}

