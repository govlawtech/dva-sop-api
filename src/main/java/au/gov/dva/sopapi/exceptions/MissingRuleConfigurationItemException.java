package au.gov.dva.sopapi.exceptions;

public class MissingRuleConfigurationItemException extends Exception {
    public MissingRuleConfigurationItemException() {
    }

    public MissingRuleConfigurationItemException(String message) {
        super(message);
    }

    public MissingRuleConfigurationItemException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingRuleConfigurationItemException(Throwable cause) {
        super(cause);
    }

    public MissingRuleConfigurationItemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
