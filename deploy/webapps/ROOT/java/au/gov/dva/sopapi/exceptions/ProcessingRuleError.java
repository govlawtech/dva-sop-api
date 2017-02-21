package au.gov.dva.sopapi.exceptions;

public class ProcessingRuleError extends DvaSopApiError {
    public ProcessingRuleError(String msg, Throwable e) {
        super(msg, e);
    }

    public ProcessingRuleError(String msg) {
        super(msg);
    }

    public ProcessingRuleError(Throwable e) {
        super(e);
    }
}
