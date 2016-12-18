package au.gov.dva.sopapi.client;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.QueryParamLabels;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponseDto;
import au.gov.dva.sopapi.dtos.sopref.SoPRefDto;
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

        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            URL serverUrl = new URL(cmd.getOptionValue("url"));
            String service = cmd.getOptionValue("service");

            SoPApiClient client = new SoPApiClient(serverUrl, service);
            String result = "";

            switch (service) {
                case "getOperations":
                    LocalDate declaredAfterDate = LocalDate.parse(cmd.getOptionValue(QueryParamLabels.QUERY_DATE));
                    OperationsResponseDto operationsResponse = client.getOperations(declaredAfterDate).get();
                    result = OperationsResponseDto.toJsonString(operationsResponse);
                    break;
                case "getSopFactors":
                    String conditionName = cmd.getOptionValue(QueryParamLabels.CONDITION_NAME);
                    String icdValue = cmd.getOptionValue(QueryParamLabels.ICD_CODE_Value);
                    String icdVersion = cmd.getOptionValue(QueryParamLabels.ICD_CODE_VERSION);
                    String incidentType = cmd.getOptionValue(QueryParamLabels.INCIDENT_TYPE);
                    String standardOfProof = cmd.getOptionValue(QueryParamLabels.STANDARD_OF_PROOF);
                    SoPRefDto sopRef = client.getFactors(conditionName, icdVersion, icdValue, incidentType, standardOfProof).get();
                    result = SoPRefDto.toJsonString(sopRef);
                default:
                    break;
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
        } finally {
            System.exit(-1);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("dva-sop-api", options);
    }

    private static Options getOptions() {
        Options options = new Options();

        // Common options
        Option serviceOption = new Option("service", true,
                "Name of the service to call.");
        serviceOption.setRequired(true);

        Option urlOption = new Option("url", true,
                "URL of the server that the API is running on.");
        urlOption.setRequired(true);

        // getOperations options
        Option declaredAfterOption = new Option(QueryParamLabels.QUERY_DATE, true,
                "Find operations declared after this ISO local date (yyyy-mm-dd).");
//        declaredAfterOption.setRequired(true);

        // getSopFactors options
        Option conditionNameOption = new Option(QueryParamLabels.CONDITION_NAME, true,
                "Name of the condition.");

        Option icdValueOption = new Option(QueryParamLabels.ICD_CODE_Value, true,
                "");

        Option icdVersionOption = new Option(QueryParamLabels.ICD_CODE_VERSION, true,
                "");

        Option incidentTypeOption = new Option(QueryParamLabels.INCIDENT_TYPE, true,
                "");

        Option standardOfProofOption = new Option(QueryParamLabels.STANDARD_OF_PROOF, true,
                "");

        OptionGroup getOperationsGroup = new OptionGroup();
        getOperationsGroup.addOption(declaredAfterOption);

        OptionGroup getSopFactorsGroup = new OptionGroup();
//        getOperationsGroup.addOption(conditionNameOption);
//        getOperationsGroup.addOption(icdValueOption);
//        getOperationsGroup.addOption(icdVersionOption);
//        getSopFactorsGroup.addOption(incidentTypeOption);
//        getSopFactorsGroup.addOption(standardOfProofOption);

//      -conditionName="lumbar spondylosis" OR BOTH
//      -icdCodeVersion="<version>"
//      -icdCodeValue="<icd code value>"
//      AND
//      -standardOfProof="RH"
//      -incidentType="onset"

        options.addOption(serviceOption);
        options.addOption(urlOption);
        options.addOption(declaredAfterOption);
        options.addOption(conditionNameOption);
        options.addOption(icdValueOption);
        options.addOption(icdVersionOption);
        options.addOption(incidentTypeOption);
        options.addOption(standardOfProofOption);

//        options.addOptionGroup(getOperationsGroup);
//        options.addOptionGroup(getSopFactorsGroup);

        return options;
    }

}
