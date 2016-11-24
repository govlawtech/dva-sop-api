package au.gov.dva;

public class AppSettings {

    public static Environment getEnvironment() {
        // todo: load from config file by naming convention

        return Environment.devtest;
    }

    public static class AzureStorage {

        public static String accountName() {
            if (getEnvironment() == Environment.devtest)
                return "devstoreaccount1";
            // todo: prod settings
            return null;
        }

        public static String accountKey() {
            if (getEnvironment() == Environment.devtest)
                return "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
            return null;
            // todo: prod settings
        }

        public static String storageConnectionString() {

            if (getEnvironment() == Environment.devtest)
                return "UseDevelopmentStorage=true";
            return null;
            // todo: prod settings
        }


    }

    public static enum Environment {
        prod,
        devtest
    }
}





