package au.gov.dva.sopref.exceptions;

public class RepositoryError extends DvaSopApiError {

    public RepositoryError(String msg, Throwable e) {
        super(msg, e);
    }

    public RepositoryError(String msg) {
        super(msg);
    }

    public RepositoryError(Throwable e) {
        super(e);
    }
}
