package au.gov.dva.sopapi;

public class ConfigurationError extends Error {
    public ConfigurationError(String msg,Throwable e)
    {
        super(msg,e);
    }

    public ConfigurationError(String msg)
    {
        super(msg);
    }

    public ConfigurationError(Throwable e)
    {
        super(e);
    }

}
