package au.gov.dva.sopref.exceptions;

public class ServiceDeterminationParserError extends DvaSopApiError {


    public ServiceDeterminationParserError(String msg, Throwable e) {
        super(msg, e);
    }

    public ServiceDeterminationParserError(String msg) {
        super(msg);
    }

    public ServiceDeterminationParserError(Throwable e) {
        super(e);
    }
}







