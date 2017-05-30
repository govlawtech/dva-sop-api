import au.gov.dva.sopapi.AppSettings;
import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.tools.StorageTool;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;

public class App {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("u", "update", false, "Check the Legislation Register and Email subscription service for SoP updates, then apply them.");
        options.addOption("i", "init", true, "Purge storage and repopulate from Legislation Register");
        options.addOption("s", "scrape", true, "Scrape Legislation Register for PDFs for given register Ids");

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            return;
        }

        RegisterClient registerClient = new FederalRegisterOfLegislationClient();
        Repository repository = new AzureStorageRepository(AppSettings.AzureStorage.getConnectionString());
        StorageTool storageTool = new StorageTool(repository, registerClient);

        if (commandLine.hasOption('u')) {
            storageTool.Update();
        }

        if (commandLine.hasOption('i')) {
            String initialSopsListFile = commandLine.getOptionValue("i");

            try {
                List<String> registerIdsOfInitialSops = getRegisterIds(initialSopsListFile);
                repository.purge();
                storageTool.SeedRuleConfig();

                storageTool.SeedStorage(registerIdsOfInitialSops);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption('s')) {
            try {

                String initialSopsListFile = commandLine.getOptionValue("s");
                Path outputPath = Files.createTempDirectory("scrapedSopPdfs_");
                List<String> registerIdsOfInitialSops = getRegisterIds(initialSopsListFile);
                FederalRegisterOfLegislationClient client = new FederalRegisterOfLegislationClient();


                registerIdsOfInitialSops.stream()
                        .forEach(
                                s -> {
                                    try {
                                        byte[] bytes = client.getAuthorisedInstrumentPdf(s).get(20, TimeUnit.SECONDS);
                                        Thread.sleep(1000); // FRL throttles when brutalized
                                        Path outputFile = Paths.get(outputPath.toString(), s + ".pdf");
                                        Files.write(outputFile, bytes, StandardOpenOption.CREATE);
                                        System.out.println("Wrote: " + outputFile.toAbsolutePath());

                                    } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
                                        System.out.println("Failed to retrieve: " + s);
                                        e.printStackTrace();
                                    }
                                }
                        );
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        System.out.println("Have a nice day.");
    }


    private static List<String> getRegisterIds(String initialSopsListFile) throws IOException {
        return Files.readAllLines(Paths.get(initialSopsListFile));
    }
}

