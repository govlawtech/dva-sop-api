package au.gov.dva.sopapi.exceptions;

public class ServiceHistoryCorruptException extends DvaSopApiRuntimeException {
    public ServiceHistoryCorruptException(String msg, Throwable e) {
        super(msg, e);
    }

    public ServiceHistoryCorruptException(String msg) {
        super(msg);
    }

    public ServiceHistoryCorruptException(Throwable e) {
        super(e);
    }
}
