package au.gov.dva.sopapi.exceptions;

public class AutoUpdateError extends DvaSopApiError {
    public AutoUpdateError(String msg, Throwable e) {
        super(msg, e);
    }

    public AutoUpdateError(String msg) {
        super(msg);
    }

    public AutoUpdateError(Throwable e) {
        super(e);
    }
}
