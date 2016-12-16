package au.gov.dva.sopapi.client;

import au.gov.dva.sopapi.dtos.sopref.OperationsResponseDto;
import org.apache.commons.cli.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if(cmd.hasOption("url")) {
                URL serverUrl = new URL(cmd.getOptionValue("url"));
                SoPApiClient client = new SoPApiClient(serverUrl);

                LocalDate declaredAfterDate = LocalDate.parse(cmd.getOptionValue("declaredAfter"));
                OperationsResponseDto operationsResponse =
                        client.getOperations(declaredAfterDate).get();

                System.out.println(operationsResponse.toJsonString(operationsResponse));
            }
        } catch (ParseException e) {
            printHelp(options);
            System.out.println(e);
        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (InterruptedException e) {
            System.out.println(e);
        } catch (ExecutionException e) {
            System.out.println(e);
        } finally {
            System.exit(-1);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("dva-sop-api", options);
    }
}
