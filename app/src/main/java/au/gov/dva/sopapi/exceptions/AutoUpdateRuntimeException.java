package au.gov.dva.sopapi.exceptions;

public class AutoUpdateRuntimeException extends DvaSopApiRuntimeException {
    public AutoUpdateRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }

    public AutoUpdateRuntimeException(String msg) {
        super(msg);
    }

    public AutoUpdateRuntimeException(Throwable e) {
        super(e);
    }
}
