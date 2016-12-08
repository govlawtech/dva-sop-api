package au.gov.dva;


import au.gov.dva.sopref.exceptions.ConfigurationError;

public class AppSettings {

    private static final String envVarName = "DEP_ENV";

    public static Environment getEnvironment() {

        String envVarValue = System.getenv(envVarName);
        if (envVarValue == null)
        {
            throw new ConfigurationError(String.format("Environment variable %smust have value.", envVarName));
        }
        switch (envVarValue)
        {
            case "devtest" : return Environment.devtest;
            case "devtestlocal" : return Environment.devtestlocal;
            case "prod" : return Environment.prod;
            default: throw new ConfigurationError(String.format("Value for environment variable %smust be 'devtest','devtestlocal' or 'prod'", envVarValue));

        }

    }

    public static class AzureStorage {

        // todo: get production values from environment variables



        public static class DevTest {
            // For use with Azure storage emulator.
            public final static String accountName = "devstoreaccount1";
            public final static String accountKey = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
            public final static String storageConnectionString =  "UseDevelopmentStorage=true";
        }
    }

    public enum Environment {
        prod,
        devtest,
        devtestlocal
    }
}





