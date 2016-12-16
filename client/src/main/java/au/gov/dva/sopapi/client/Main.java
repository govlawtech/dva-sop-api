package au.gov.dva.sopapi.client;

import org.apache.commons.cli.*;

import java.net.URL;

public class Main {

    public static void main(String[] args)
    {
        // todo:
        // - parse command line options
        // - instantiate SoPApiClient with correct URL
        // - execute SoPApiClient methods and print results to console

        Options options = new Options();

        // Common options
        Option serviceOption = new Option("service", true, "Name of the service to call.");
        serviceOption.setRequired(true);

        Option urlOption = new Option("url", true, "URL of the server that the API is running on.");
        urlOption.setRequired(true);

        // getOperations options
        Option declaredAfterOption = new Option("declaredAfter", true, "Find operations declared after this ISO local date (yyyy-mm-dd).");
        declaredAfterOption.setRequired(true);

        options.addOption(serviceOption);
        options.addOption(urlOption);
        options.addOption(declaredAfterOption);

    }
}
