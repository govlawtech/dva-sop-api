package au.gov.dva;


public class AppSettings {

    public static Environment getEnvironment() {

        return Environment.devtest;
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

    public static enum Environment {
        prod,
        devtest
    }
}





