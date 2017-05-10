package au.gov.dva.sopapi;

import au.gov.dva.sopapi.exceptions.DvaSopApiError;

public class ConfigurationError extends DvaSopApiError {
    public ConfigurationError(String msg, Throwable e) {
        super(msg, e);
    }

    public ConfigurationError(String msg) {
        super(msg);
    }

    public ConfigurationError(Throwable e) {
        super(e);
    }
}
