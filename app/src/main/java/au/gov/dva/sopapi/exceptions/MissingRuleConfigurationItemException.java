package au.gov.dva.sopapi.exceptions;

public class MissingRuleConfigurationItemException extends DvaSopApiRuntimeException {

    public MissingRuleConfigurationItemException(String msg, Throwable e) {
        super(msg, e);
    }

    public MissingRuleConfigurationItemException(String msg) {
        super(msg);
    }

    public MissingRuleConfigurationItemException(Throwable e) {
        super(e);
    }
}
