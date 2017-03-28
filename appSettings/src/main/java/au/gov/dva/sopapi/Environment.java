package au.gov.dva.sopapi;



public enum Environment {
    prod,
    devtest,
    devtestlocal;

    public boolean isDev()
    {
        return this.equals(devtest) || this.equals(devtestlocal);
    }
}
