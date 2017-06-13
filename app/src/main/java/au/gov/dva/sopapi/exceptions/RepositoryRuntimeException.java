package au.gov.dva.sopapi.exceptions;

public class RepositoryRuntimeException extends DvaSopApiRuntimeException {

    public RepositoryRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }

    public RepositoryRuntimeException(String msg) {
        super(msg);
    }

    public RepositoryRuntimeException(Throwable e) {
        super(e);
    }
}
