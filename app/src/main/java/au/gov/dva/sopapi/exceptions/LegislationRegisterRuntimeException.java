package au.gov.dva.sopapi.exceptions;

public class LegislationRegisterRuntimeException extends DvaSopApiRuntimeException {

    public LegislationRegisterRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }

    public LegislationRegisterRuntimeException(String msg) {
        super(msg);
    }

    public LegislationRegisterRuntimeException(Throwable e) {
        super(e);
    }
}
