package au.gov.dva.sopapi.exceptions;

public class DvaSopApiRuntimeException extends RuntimeException {
    public DvaSopApiRuntimeException(String msg, Throwable e)
    {
        super(msg,e);
    }

    public DvaSopApiRuntimeException(String msg)
    {
        super(msg);
    }

    public DvaSopApiRuntimeException(Throwable e)
    {
        super(e);
    }

}
