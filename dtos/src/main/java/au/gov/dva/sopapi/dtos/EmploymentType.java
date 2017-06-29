package au.gov.dva.sopapi.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EmploymentType {
    CFTS,
    NON_CFTS;

    private final static String CFTS_LABEL = "Regular/Permanent Force";
    private final static String NON_CFTS_LABEL = "Non-Regular/Permanent Force";

    @Override
    public String toString() {
        switch (this)
        {
            case CFTS: return CFTS_LABEL;
            case NON_CFTS: return NON_CFTS_LABEL;
            default: throw new IllegalArgumentException();
        }
    }

    @JsonCreator
    public static EmploymentType fromString(String value)
    {
        switch (value)
        {
            case CFTS_LABEL: return CFTS;
            case NON_CFTS_LABEL: return NON_CFTS;
            default: return NON_CFTS;
        }
    }
}
