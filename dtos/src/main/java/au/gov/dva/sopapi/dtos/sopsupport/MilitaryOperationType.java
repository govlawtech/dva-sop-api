package au.gov.dva.sopapi.dtos.sopsupport;

public enum MilitaryOperationType {
    Warlike("warlike"),
    NonWarlike("non-warlike"),
    Hazardous("hazardous"),
    Peacekeeping("peacekeeping");

    private final String text;

    @Override
    public String toString() {
        return text;
    }

    MilitaryOperationType(String text)
    {
        this.text = text;
    }

}
