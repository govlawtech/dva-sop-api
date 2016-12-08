package au.gov.dva;


import au.gov.dva.sopref.exceptions.ConfigurationError;

import static au.gov.dva.AppSettings.Environment.devtest;

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
            case "devtest" : return devtest;
            case "devtestlocal" : return Environment.devtestlocal;
            case "prod" : return Environment.prod;
            default: throw new ConfigurationError(String.format("Value for environment variable %smust be 'devtest','devtestlocal' or 'prod'", envVarValue));

        }
    }

    private static String getEnvVarValue(String envVarName)
    {
        String value = System.getenv(envVarName);
        if (value == null)
        {
            throw new ConfigurationError(String.format("Expecting value for environment variable: %s", envVarName));
        }
        return value;
    }

    public static class AzureStorage {

        // todo: get production values from environment variables

        private static final String AZURE_STORAGE_CONNECTION_STRING = "AZURE_STORAGE_CONNECTION_STRING";

        public static String getConnectionString() {
            switch (getEnvironment())
            {
                case devtestlocal: return DevTestLocal.storageConnectionString;
                case devtest: return getEnvVarValue(AZURE_STORAGE_CONNECTION_STRING);
                case prod: return getEnvVarValue(AZURE_STORAGE_CONNECTION_STRING);
                default: throw new ConfigurationError("Cannot get Azure connection string");
            }
        }



        private static class DevTestLocal {
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





