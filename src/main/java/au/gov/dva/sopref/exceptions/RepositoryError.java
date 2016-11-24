package au.gov.dva.sopref.exceptions;

public class RepositoryError extends Error {
    public RepositoryError(String msg) {super(msg);}
    public RepositoryError(Throwable e) {super(e);}
    public RepositoryError(String msg, Throwable e) {super(msg,e);}

}
