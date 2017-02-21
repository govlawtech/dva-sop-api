package au.gov.dva.sopapi.exceptions;

public class SopParserError extends DvaSopApiError {

    public SopParserError(String msg, Throwable e) {
        super(msg, e);
    }

    public SopParserError(String msg) {
        super(msg);
    }

    public SopParserError(Throwable e) {
        super(e);
    }
}
