package au.gov.dva.sopapi.dtos;

public class DvaSopApiDtoError extends Error {
    public DvaSopApiDtoError(String msg, Throwable e)
    {
        super(msg,e);
    }

    public DvaSopApiDtoError(String msg)
    {
        super(msg);
    }

    public DvaSopApiDtoError(Throwable e)
    {
        super(e);
    }
}
