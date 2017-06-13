package au.gov.dva.sopapi.exceptions;

public class ProcessingRuleRuntimeException extends DvaSopApiRuntimeException {
    public ProcessingRuleRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }

    public ProcessingRuleRuntimeException(String msg) {
        super(msg);
    }

    public ProcessingRuleRuntimeException(Throwable e) {
        super(e);
    }
}
