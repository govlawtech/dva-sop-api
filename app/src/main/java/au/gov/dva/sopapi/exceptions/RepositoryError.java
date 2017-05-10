package au.gov.dva.sopapi.exceptions;

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
