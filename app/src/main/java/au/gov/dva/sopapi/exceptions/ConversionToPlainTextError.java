package au.gov.dva.sopapi.exceptions;

public class ConversionToPlainTextError extends DvaSopApiError {

    public ConversionToPlainTextError(String msg, Throwable e) {
        super(msg, e);
    }

    public ConversionToPlainTextError(String msg) {
        super(msg);
    }

    public ConversionToPlainTextError(Throwable e) {
        super(e);
    }
}
