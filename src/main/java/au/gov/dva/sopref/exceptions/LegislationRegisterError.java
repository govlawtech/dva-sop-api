package au.gov.dva.sopref.exceptions;

public class LegislationRegisterError extends Error {

    public LegislationRegisterError(String msg,Throwable e)
    {
        super(msg,e);
    }

    public LegislationRegisterError(String msg)
    {
        super(msg);
    }

    public LegislationRegisterError(Throwable e)
    {
        super(e);
    }
}







