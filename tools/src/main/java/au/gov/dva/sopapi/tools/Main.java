package au.gov.dva.sopapi.tools;

import au.gov.dva.sopapi.AppSettings;
import au.gov.dva.sopapi.ConfigurationRuntimeException;
import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.sopsupport.ruleconfiguration.CsvRuleConfigurationRepository;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {

        Option sopsFilePath = new Option("sops", "sopIds", true, "Path to line delimited text file of Register IDs of instruments on Federal Register of Legislation.");
        Option helpOption = new Option("h", "help", false, "Print help");
        Option updateOption = new Option("u", "update", false, "Check the Legislation Register and Email subscription service for SoP updates, then apply them.");
        Option purgeOption = new Option("p", "purge", false, "Purge storage.");
        Option initSopsOption = new Option("is", "initSops", false, "Initialize storage with sops.");
        Option initRulesOption = new Option("ir", "initRules", false, "Initialize storage with rule config.");
        Option validateRuleConfigOption = new Option("v", "validateRuleConfig",false,"Validate rule config only, do not upload.");
        Option rhRulesOption = new Option("rh", "RH", true, "Path to CSV containing RH rules");
        Option bopRulesOption = new Option("bop", "BoP", true, "Path to CSV containing BoP rules");
        Option scrapeOption = new Option("scrape", "scrape", false, "Scrape Legislation Register for PDFs for given register ids.");
        Option getCurrentSops = new Option("getCurrentSops","getCurrentSops",false,"Get a list of the register IDs of all SoPs in storage.");

        Options options = new Options()
                .addOption(sopsFilePath)
                .addOption(helpOption)
                .addOption(updateOption)
                .addOption(purgeOption)
                .addOption(initSopsOption)
                .addOption(initRulesOption)
                .addOption(rhRulesOption)
                .addOption(bopRulesOption)
                .addOption(validateRuleConfigOption)
                .addOption(scrapeOption)
                .addOption(getCurrentSops);

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            return;
        }
        if (commandLine.hasOption("h")) {
           printHelp(options);
           return;
        }

        RegisterClient registerClient = new FederalRegisterOfLegislationClient();
        Repository repository = new AzureStorageRepository(AppSettings.AzureStorage.getConnectionString());
        au.gov.dva.sopapi.tools.StorageTool storageTool = new au.gov.dva.sopapi.tools.StorageTool(repository, registerClient);

        if (commandLine.hasOption("getCurrentSops"))
        {
            Stream<String> registerIds = repository.getAllSops().stream()
                    .map(soP -> soP.getRegisterId());

            System.out.println("Register IDs in repository (note: some may be end-dated):");
            registerIds.forEach(s -> System.out.println(s));
        }

        if (commandLine.hasOption("p"))
        {
            repository.purge();
            System.out.println("Repository purged.");
        }

        if (commandLine.hasOption("is")) {
            if (!commandLine.hasOption("sops"))
            {
                System.out.println("Missing arg: " + sopsFilePath.getArgName());
                return;
            }
            String initialSopsListFile = commandLine.getOptionValue("sops");

            try {
                List<String> registerIdsOfInitialSops = getRegisterIds(initialSopsListFile);
                storageTool.SeedStorageWithSops(registerIdsOfInitialSops);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption("ir")) {
            if ( !commandLine.hasOption("rh") || !commandLine.hasOption("bop"))
            {
                System.out.println(String.format("Required args: %s,%s",rhRulesOption.getArgName(),bopRulesOption.getArgName()));
                return;
            }

            try {
                byte[] rh = Files.readAllBytes(Paths.get(commandLine.getOptionValue("rh")));
                byte[] bop = Files.readAllBytes(Paths.get(commandLine.getOptionValue("bop")));


                Boolean configValid = isRuleConfigValid(rh,bop);
                if (configValid) {
                    if (!commandLine.hasOption("v")) {
                        storageTool.SeedRuleConfig(rh, bop);
                        System.out.println("Successfully set rule config.");
                    }
                    else {
                        System.out.println("Rule config validated.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }


        }

        if (commandLine.hasOption("scrape")) {
            try {

                String initialSopsListFile = commandLine.getOptionValue("sops");
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

        if (commandLine.hasOption('u')) {
            storageTool.Update();
        }

        System.out.println("Have a nice day.");
    }


    private static List<String> getRegisterIds(String initialSopsListFile) throws IOException {
        return Files.readAllLines(Paths.get(initialSopsListFile));
    }

    private static boolean isRuleConfigValid(byte[] rhCsv, byte[] bopCsv)
    {
        try {
            CsvRuleConfigurationRepository csvRuleConfigurationRepository = new CsvRuleConfigurationRepository(rhCsv, bopCsv);
            return true;
        }
        catch (ConfigurationRuntimeException e)
        {
            System.out.println(e);
            return false;
        }
    }

    private static void printHelp(Options options)
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("dsacli", options);
    }
}

