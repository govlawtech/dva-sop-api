package au.gov.dva.sopapi.exceptions;

public class InitialSeedingError extends DvaSopApiError{
    public InitialSeedingError(String msg, Throwable e) {
        super(msg, e);
    }

    public InitialSeedingError(String msg) {
        super(msg);
    }

    public InitialSeedingError(Throwable e) {
        super(e);
    }
}
