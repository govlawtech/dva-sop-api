package au.gov.dva.sopapi;

import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException;

public class ConfigurationRuntimeException extends DvaSopApiRuntimeException {
    public ConfigurationRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }

    public ConfigurationRuntimeException(String msg) {
        super(msg);
    }

    public ConfigurationRuntimeException(Throwable e) {
        super(e);
    }
}
