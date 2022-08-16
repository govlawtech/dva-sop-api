package au.gov.dva.sopapi.interfaces.model;

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
}
