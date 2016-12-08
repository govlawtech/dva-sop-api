package au.gov.dva.sopref.exceptions;

public class OperationParserError extends Error {

    public OperationParserError(String msg, Throwable e)
    {
        super(msg,e);
    }

    public OperationParserError(String msg)
    {
        super(msg);
    }

    public OperationParserError(Throwable e)
    {
        super(e);
    }

}







