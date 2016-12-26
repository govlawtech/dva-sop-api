package au.gov.dva.sopapi.client;

import au.gov.dva.sopapi.dtos.QueryParamLabels;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponseDto;
import au.gov.dva.sopapi.dtos.sopref.SoPRefDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import com.google.common.base.Charsets;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            URL serverUrl = new URL(cmd.getOptionValue("url"));
            String service = cmd.getOptionValue("service");
            SoPApiClient client = new SoPApiClient(serverUrl);
            String result;

            List<String> missingOptions = new ArrayList<>();

            switch (service) {
                case "operations":

                    OperationsResponseDto operationsResponse = client.getOperations().get();
                    result = OperationsResponseDto.toJsonString(operationsResponse);

                    break;
                case "factors":
                    // Condition and both ICD value and version are missing
                    if (!cmd.hasOption(QueryParamLabels.CONDITION_NAME)
                            && !cmd.hasOption(QueryParamLabels.ICD_CODE_VALUE)
                            && !cmd.hasOption(QueryParamLabels.ICD_CODE_VERSION)) {

                        missingOptions.add(QueryParamLabels.CONDITION_NAME);
                        missingOptions.add(QueryParamLabels.ICD_CODE_VALUE);
                        missingOptions.add(QueryParamLabels.ICD_CODE_VERSION);
                    }

                    // ICD version is missing
                    if (cmd.hasOption(QueryParamLabels.ICD_CODE_VALUE)) {
                        if (!cmd.hasOption(QueryParamLabels.ICD_CODE_VERSION)) {
                            missingOptions.add(QueryParamLabels.ICD_CODE_VERSION);
                        }
                    }

                    // ICD value is missing
                    if (cmd.hasOption((QueryParamLabels.ICD_CODE_VERSION))) {
                        if (!cmd.hasOption((QueryParamLabels.ICD_CODE_VALUE))) {
                            missingOptions.add(QueryParamLabels.ICD_CODE_VALUE);
                        }
                    }

                    if (!cmd.hasOption(QueryParamLabels.INCIDENT_TYPE)) {
                        missingOptions.add(QueryParamLabels.INCIDENT_TYPE);
                    }

                    if (!cmd.hasOption(QueryParamLabels.STANDARD_OF_PROOF)) {
                        missingOptions.add(QueryParamLabels.STANDARD_OF_PROOF);
                    }

                    if (!missingOptions.isEmpty()) {
                        throw new MissingOptionException(missingOptions);
                    }

                    String conditionName = cmd.getOptionValue(QueryParamLabels.CONDITION_NAME);
                    String icdValue = cmd.getOptionValue(QueryParamLabels.ICD_CODE_VALUE);
                    String icdVersion = cmd.getOptionValue(QueryParamLabels.ICD_CODE_VERSION);
                    String incidentType = cmd.getOptionValue(QueryParamLabels.INCIDENT_TYPE);
                    String standardOfProof = cmd.getOptionValue(QueryParamLabels.STANDARD_OF_PROOF);
                    SoPRefDto sopRef = client.getFactors(conditionName, icdVersion, icdValue, incidentType, standardOfProof).get();
                    result = SoPRefDto.toJsonString(sopRef);

                    break;
                case "process":

                    if (!cmd.hasOption(ParamNames.PROCESSING_REQUEST_PATH)) {
                        missingOptions.add(ParamNames.PROCESSING_REQUEST_PATH);
                    }
                    if (!missingOptions.isEmpty()) {
                        throw new MissingOptionException(missingOptions);
                    }
                    String path = cmd.getOptionValue(ParamNames.PROCESSING_REQUEST_PATH);
                    String content = readFile(path, Charsets.UTF_8);

                    SopSupportResponseDto response = client.getSatisfiedFactors(content).get();
                    System.out.println(SopSupportResponseDto.toJsonString(response));


                default:
                    throw new ParseException("Unrecognised value for 'service' option");

            }

            System.out.println(result);
        } catch (ParseException e) {
            printHelp(options);
            System.out.println(e);
        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (InterruptedException e) {
            System.out.println(e);
        } catch (ExecutionException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            System.exit(-1);
        }
    }

    private static class ParamNames {
        public final static String PROCESSING_REQUEST_PATH = "r";
    }

    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        // todo: add missing service
        helpFormatter.printHelp("[-" +
                QueryParamLabels.CONDITION_NAME + " <name> | -" +
                QueryParamLabels.ICD_CODE_VALUE + " <value> -" +
                QueryParamLabels.ICD_CODE_VERSION + " <version>] -" +
                QueryParamLabels.INCIDENT_TYPE + " <type> -" +
                QueryParamLabels.STANDARD_OF_PROOF + " <proof>]]", options);
    }

    private static Options getOptions() {
        Options options = new Options();

        // Common options
        Option serviceOption = new Option("service", true,
                "Name of the service to call: 'factors', 'operations' or 'process'.");
        serviceOption.setRequired(true);

        Option urlOption = new Option("url", true,
                "URL of the server that the API is running on.");
        urlOption.setRequired(true);

        // getSopFactors options
        Option conditionNameOption = new Option(QueryParamLabels.CONDITION_NAME, true,
                "Name of the condition.");

        Option icdValueOption = new Option(QueryParamLabels.ICD_CODE_VALUE, true,
                "International Classification of Diseases code (eg. M47.01).");

        Option icdVersionOption = new Option(QueryParamLabels.ICD_CODE_VERSION, true,
                "International Classification of Diseases version (eg. ICD-10-AM).");

        Option incidentTypeOption = new Option(QueryParamLabels.INCIDENT_TYPE, true,
                "Indicates the 'onset' or 'aggravation' of the condition.");

        Option standardOfProofOption = new Option(QueryParamLabels.STANDARD_OF_PROOF, true,
                "Standard of proof for determining liability (eg. RH or BoP)");


        // connectionToService
        Option requestBodyFile = new Option("r", "request", true, "Path to JSON file containing the request body.");

        options.addOption(serviceOption);
        options.addOption(urlOption);
        options.addOption(conditionNameOption);
        options.addOption(icdValueOption);
        options.addOption(icdVersionOption);
        options.addOption(incidentTypeOption);
        options.addOption(standardOfProofOption);
        options.addOption(requestBodyFile);


        return options;
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
