package au.gov.dva.sopapi;


import static au.gov.dva.sopapi.Environment.*;

public class AppSettings {


    private static final String envVarName = "DEP_ENV";
    private static final String deploymentDatesVerificationFeatureToggle = "VALIDATE_DEPLOYMENT_DATES";

    public static Boolean isEnvironmentSet(){
        return (System.getProperty(envVarName) != null || System.getenv(envVarName) != null);
    }

    public static Environment getEnvironment() {

        String jvmArg = System.getProperty(envVarName);

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


    public static String getCacheRefreshKey()
    {
        String key;
        try {
            key = getPropertyValue("CACHE_REFRESH_KEY");
            return key;
        }
        catch (ConfigurationError e)
        {
            return null;
        }

    }

    public static Boolean shouldValidateDeploymentDatesForStp() {
        String value = getPropertyValue(deploymentDatesVerificationFeatureToggle);
        return value.toLowerCase().equals("true");
    }

    private static Environment convertEnvironmentStringEnum(String environmentStringValue)
    {
        switch (environmentStringValue)
        {
            case "devtestlocal" : return  devtestlocal;
            case "devtest" : return devtest;
            case "prod" : return prod;
            case "dev": return dev;
            default: throw new ConfigurationError(String.format("Value for environment variable %s must be 'dev', 'devtest','devtestlocal' or 'prod'.  Current value: '%s'.", envVarName, environmentStringValue));
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

    public static String getBaseUrl() {
        return getPropertyValue("BASE_URL");
    }

    public static class LegislationRegisterEmailSubscription {
        private static final String LEGISLATION_REGISTER_SUBSCRIPTION_EMAIL_USER_ID = "LRS_USERID";
        private static final String LEGISLATION_REGISTER_SUBSCRIPTION_EMAIL_PASSWORD = "LRS_PASSWORD";
        public static String getUserId() {
            return getPropertyValue(LEGISLATION_REGISTER_SUBSCRIPTION_EMAIL_USER_ID);
        }

        public static String getPassword(){
            return getPropertyValue(LEGISLATION_REGISTER_SUBSCRIPTION_EMAIL_PASSWORD);
        }
    }

    public static class AzureStorage {

        private static final String AZURE_STORAGE_CONNECTION_STRING = "AZURE_STORAGE_CONNECTION_STRING";

        public static String getConnectionString() {

            switch (getEnvironment())
            {
                case devtestlocal: return DevTestLocal.storageConnectionString;
                case devtest: return getPropertyValue(AZURE_STORAGE_CONNECTION_STRING);
                case prod: return getPropertyValue(AZURE_STORAGE_CONNECTION_STRING);
                case dev: return getPropertyValue(AZURE_STORAGE_CONNECTION_STRING);
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


}





