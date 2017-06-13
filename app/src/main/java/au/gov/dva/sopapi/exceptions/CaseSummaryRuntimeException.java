package au.gov.dva.sopapi.exceptions;

public class CaseSummaryRuntimeException extends DvaSopApiRuntimeException {

    public CaseSummaryRuntimeException(String msg) { super(msg); }
    public CaseSummaryRuntimeException(Throwable e) { super(e); }
    public CaseSummaryRuntimeException(String msg, Throwable e) { super(msg, e); }

}
