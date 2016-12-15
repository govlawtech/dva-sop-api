package au.gov.dva.sopref.exceptions;

public class DvaSopApiError extends Error {
    public DvaSopApiError(String msg,Throwable e)
    {
        super(msg,e);
    }

    public DvaSopApiError(String msg)
    {
        super(msg);
    }

    public DvaSopApiError(Throwable e)
    {
        super(e);
    }

}
