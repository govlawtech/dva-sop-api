package au.gov.dva.sopapi.dtos;

public class DvaSopApiDtoRuntimeException extends RuntimeException {
    public DvaSopApiDtoRuntimeException(String msg, Throwable e)
    {
        super(msg,e);
    }

    public DvaSopApiDtoRuntimeException(String msg)
    {
        super(msg);
    }

    public DvaSopApiDtoRuntimeException(Throwable e)
    {
        super(e);
    }
}
