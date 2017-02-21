package au.gov.dva.sopapi.exceptions;

public class CaseSummaryError extends Error {

    public CaseSummaryError(String msg) { super(msg); }
    public CaseSummaryError(Throwable e) { super(e); }
    public CaseSummaryError(String msg, Throwable e) { super(msg, e); }

}
