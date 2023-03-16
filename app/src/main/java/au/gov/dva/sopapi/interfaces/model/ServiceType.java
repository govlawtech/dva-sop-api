package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.sopsupport.MilitaryOperationType;

public enum ServiceType {
    WARLIKE("warlike"),
    NON_WARLIKE("non-warlike"),
    PEACETIME("peacetime");

    public static ServiceType fromText(String text)
    {
        if (text.contentEquals("warlike"))
            return ServiceType.WARLIKE;
        if (text.contentEquals("non-warlike"))
            return ServiceType.NON_WARLIKE;
        if (text.contentEquals("peacetime"))
            return ServiceType.PEACETIME;
        else throw new IllegalArgumentException(String.format("Unrecognised service type: %s", text));
    }

    public boolean isOperational()
    {
        return (this == ServiceType.WARLIKE || this == ServiceType.NON_WARLIKE);
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

    public MilitaryOperationType toMilitaryOperationType()
    {
        if (this == ServiceType.WARLIKE)
            return MilitaryOperationType.Warlike;
        else if (this == ServiceType.NON_WARLIKE)
            return MilitaryOperationType.NonWarlike;
        else if (this == ServiceType.PEACETIME);
            return MilitaryOperationType.Peacekeeping;
    }

}
