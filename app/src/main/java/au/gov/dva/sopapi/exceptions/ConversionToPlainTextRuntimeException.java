package au.gov.dva.sopapi.exceptions;

public class ConversionToPlainTextRuntimeException extends DvaSopApiRuntimeException {

    public ConversionToPlainTextRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }

    public ConversionToPlainTextRuntimeException(String msg) {
        super(msg);
    }

    public ConversionToPlainTextRuntimeException(Throwable e) {
        super(e);
    }
}
