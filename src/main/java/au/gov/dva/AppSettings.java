package au.gov.dva;

public class AppSettings {

    public static Environment getEnvironment() {
        // todo: load from config file by naming convention
        return Environment.devtest;
    }

    public enum Environment {
        prod,
        devtest
    }
}


