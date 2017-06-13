package au.gov.dva.sopapi.exceptions;

public class SopParserRuntimeException extends DvaSopApiRuntimeException {

    public SopParserRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }

    public SopParserRuntimeException(String msg) {
        super(msg);
    }

    public SopParserRuntimeException(Throwable e) {
        super(e);
    }
}
