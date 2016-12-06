package au.gov.dva.sopref.exceptions;

public class SopParserError extends Error {

    public SopParserError(String msg, Throwable e)
    {
        super(msg,e);
    }

    public SopParserError(String msg)
    {
        super(msg);
    }

    public SopParserError(Throwable e)
    {
        super(e);
    }
}
