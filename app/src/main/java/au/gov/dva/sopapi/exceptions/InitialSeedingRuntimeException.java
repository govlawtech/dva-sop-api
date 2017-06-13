package au.gov.dva.sopapi.exceptions;

public class InitialSeedingRuntimeException extends DvaSopApiRuntimeException {
    public InitialSeedingRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }

    public InitialSeedingRuntimeException(String msg) {
        super(msg);
    }

    public InitialSeedingRuntimeException(Throwable e) {
        super(e);
    }
}
