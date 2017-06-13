package au.gov.dva.sopapi.exceptions;

public class ServiceDeterminationParserRuntimeException extends DvaSopApiRuntimeException {


    public ServiceDeterminationParserRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }

    public ServiceDeterminationParserRuntimeException(String msg) {
        super(msg);
    }

    public ServiceDeterminationParserRuntimeException(Throwable e) {
        super(e);
    }
}







