package au.gov.dva.sopapi.exceptions;

public class LegislationRegisterError extends DvaSopApiError {

    public LegislationRegisterError(String msg, Throwable e) {
        super(msg, e);
    }

    public LegislationRegisterError(String msg) {
        super(msg);
    }

    public LegislationRegisterError(Throwable e) {
        super(e);
    }
}
