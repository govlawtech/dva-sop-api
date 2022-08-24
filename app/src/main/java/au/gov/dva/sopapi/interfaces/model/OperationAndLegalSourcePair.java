package au.gov.dva.sopapi.interfaces.model;

import java.util.Objects;

public  class OperationAndLegalSourcePair {
    private String _legalSource;
    private Operation _operation;
    public OperationAndLegalSourcePair(String legalSource, Operation operation)
    {
        _legalSource = legalSource;
        _operation = operation;
    }
    public String get_legalSource() {return _legalSource;}
    public Operation get_operation() {return _operation;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationAndLegalSourcePair that = (OperationAndLegalSourcePair) o;
        return Objects.equals(_legalSource, that._legalSource) && Objects.equals(_operation, that._operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_legalSource, _operation);
    }
}
