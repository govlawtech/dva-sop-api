package au.gov.dva.sopapi.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EmploymentType {
    CTFS,
    NON_CTFS;

    private final static String CFTS_LABEL = "Regular/Permanent Force";
    private final static String NON_CTFS_LABEL = "Non-Regular/Permanent Force";

    @Override
    public String toString() {
        switch (this)
        {
            case CTFS: return CFTS_LABEL;
            case NON_CTFS: return NON_CTFS_LABEL;
            default: throw new IllegalArgumentException();
        }
    }

    @JsonCreator
    EmploymentType fromString(String value)
    {
        switch (value)
        {
            case CFTS_LABEL: return CTFS;
            case NON_CTFS_LABEL: return NON_CTFS;
            default: return NON_CTFS;
        }
    }
}
