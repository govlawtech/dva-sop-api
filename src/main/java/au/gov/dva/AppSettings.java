package au.gov.dva;


import au.gov.dva.exceptions.ConfigurationError;

import static au.gov.dva.AppSettings.Environment.devtest;
import static au.gov.dva.AppSettings.Environment.devtestlocal;
import static au.gov.dva.AppSettings.Environment.prod;

public class AppSettings {

    private static final String envVarName = "DEP_ENV";

    public static Environment getEnvironment() {

        String jvmArg = System.getProperty("DEP_ENV");

        if (jvmArg != null) {
            return convertEnvironmentStringEnum(jvmArg);
        }

        else {

            String envVarValue = System.getenv(envVarName);
            if (envVarValue == null) {
                throw new ConfigurationError(String.format("Environment variable %s must have value.", envVarName));
            }
            return convertEnvironmentStringEnum(envVarValue);
        }
    }

    private static Environment convertEnvironmentStringEnum(String environmentStringValue)
    {
        switch (environmentStringValue)
        {
            case "devtestlocal" : return devtestlocal;
            case "devtest" : return devtest;
            case "prod" : return prod;
            default: throw new ConfigurationError(String.format("Value for environment variable %smust be 'devtest','devtestlocal' or 'prod'", environmentStringValue));
        }
    }

    private static String getPropertyValue(String propertyName)
    {
        String jvmArg = System.getProperty(propertyName);
        if (jvmArg != null)
        {
            return jvmArg;
        }

        else {
            String value = System.getenv(propertyName);
            if (value == null) {
                throw new ConfigurationError(String.format("Expecting value for environment variable: %s", propertyName));
            }
            return value;
        }

    }

    public static class AzureStorage {

        // todo: get production values from environment variables

        private static final String AZURE_STORAGE_CONNECTION_STRING = "AZURE_STORAGE_CONNECTION_STRING";

        public static String getConnectionString() {

            switch (getEnvironment())
            {
                case devtestlocal: return DevTestLocal.storageConnectionString;
                case devtest: return getPropertyValue(AZURE_STORAGE_CONNECTION_STRING);
                case prod: return getPropertyValue(AZURE_STORAGE_CONNECTION_STRING);
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





