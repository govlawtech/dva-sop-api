package au.gov.dva;

import au.gov.dva.sopref.GetSopFactors;
import au.gov.dva.sopref.data.AzureStorageRepository;
import au.gov.dva.sopref.data.sops.BasicICDCode;
import au.gov.dva.sopref.interfaces.Repository;
import au.gov.dva.sopref.interfaces.model.IncidentType;
import au.gov.dva.sopref.interfaces.model.SoP;
import au.gov.dva.sopref.interfaces.model.StandardOfProof;
import com.google.common.collect.ImmutableSet;
import spark.QueryParamsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class Application implements spark.servlet.SparkApplication {



    private ImmutableSet<SoP> _allSops;

    public Application() {
        Repository repository = new AzureStorageRepository(AppSettings.AzureStorage.getConnectionString());
        _allSops = repository.getAllSops();
    }


    private static class QueryParamLabels {
        public static final String ICD_CODE_VERSION = "icdCodeVersion";
        public static final String ICD_CODE_Value = "icdCodeValue";
        public static final String CONDITION_NAME = "conditionName";
        public static final String STANDARD_OF_PROOF = "standardOfProof";
        public static final String INCIDENT_TYPE = "incidentType";

    }

    @Override
    public void init() {
        get("/hello", (req, res) -> "Hello World ");


        // todo: set headers as per spec
        get("/getSopFactors",(req,res) -> {
             QueryParamsMap queryParamsMap =  req.queryMap();
             String icdCodeValue = queryParamsMap.get("icdCodeValue").value();
             String icdCodeVersion = queryParamsMap.get("icdCodeVersion").value();
             String standardOfProof = queryParamsMap.get("standardOfProof").value();
             String conditionName = queryParamsMap.get("conditionName").value();
             String incidentType = queryParamsMap.get("incidentType").value();

             List<String> errors = getParamsValidationErrors(icdCodeValue,icdCodeVersion,standardOfProof,conditionName,incidentType);

             if (errors.size() > 0)
             {
                res.status(400);
                res.type("text/plain");
                return "Your request is malformed: \r\n\r\n" +  String.join("\r\n",errors);
             }

             ImmutableSet<SoP> matchingSops = GetSopFactors.getMatchingSops(conditionName,new BasicICDCode(icdCodeVersion,icdCodeValue), _allSops);

             if (matchingSops.isEmpty())
             {
                 res.status(404);
                 res.type("text/plain");
                 return buildErrorMessageShowingRecognisedIcdCodesAndConditionNames(_allSops);
             }

             else {

                 res.status(200);
                 res.type("application/json");

                 IncidentType it = IncidentType.fromString(incidentType);
                 StandardOfProof sp = StandardOfProof.fromAbbreviation(standardOfProof);

                 String response =  GetSopFactors.buildSopRefJsonResponse(matchingSops, it, sp);
                 return response;
             }
        });
     }

     private static List<String> getParamsValidationErrors(String icdCodeValue, String icdCodeVersion, String standardOfProof, String conditionname, String incidentType)
     {
         List<String> errors = new ArrayList<>();

         if (conditionname == null)
         {
             String missingICDCodeError = "Need ICD code (query parameter '" + QueryParamLabels.ICD_CODE_Value  + "') and ICD code version (query paramater '" + QueryParamLabels.ICD_CODE_VERSION + "') if condition name (query parameter '" + QueryParamLabels.CONDITION_NAME + "') is not provided.";
             if (icdCodeValue == null)
                 errors.add(buildQueryParamErrorMessage(QueryParamLabels.ICD_CODE_Value,missingICDCodeError));

             if (icdCodeVersion == null)
             {
                 errors.add(buildQueryParamErrorMessage(QueryParamLabels.ICD_CODE_VERSION,missingICDCodeError));
             }
         }

         if (standardOfProof == null)
             errors.add(buildQueryParamErrorMessage(QueryParamLabels.STANDARD_OF_PROOF, "required, missing."));

         else {
             if (!standardOfProof.contentEquals("RH") && !standardOfProof.contentEquals("BoP") )
                 errors.add(buildQueryParamErrorMessage(QueryParamLabels.STANDARD_OF_PROOF, "acceptable values are 'RH' (for Reasonable Hypthesis) and 'BoP' (for Balance of Probabilities)."));
         }

         if (incidentType == null)
             errors.add(buildQueryParamErrorMessage(QueryParamLabels.INCIDENT_TYPE, "required, missing."));
         else {
             if (!incidentType.contentEquals("aggravation") && !incidentType.contentEquals("onset"))
                 errors.add(buildQueryParamErrorMessage(QueryParamLabels.INCIDENT_TYPE, "acceptable values are 'aggravation' and 'onset'."));
         }

         return errors;
     }


     private static String buildQueryParamErrorMessage(String queryParamName, String msg)
     {
         return String.format("* Query paramater '%s': %s", queryParamName, msg);
     }

     private static  String buildErrorMessageShowingRecognisedIcdCodesAndConditionNames(ImmutableSet<SoP> sops)
     {
         String recognisedConditionNames = String.join("\r\n",sops.stream().map(soP -> "* " + soP.getConditionName()).sorted().collect(Collectors.toList()));

         String recognisedICDCodes = String.join("\r\n",sops.stream().flatMap(soP -> soP.getICDCodes().stream())
                 .map(code -> String.format("* %s %s", code.getVersion(), code.getCode()))
                 .collect(Collectors.toList()));

         StringBuilder sb = new StringBuilder();
         sb.append("The condition name and ICD code (if any) you provided did not match any in the database.\r\n\r\n");
         sb.append("Known condition names:\r\n");
         sb.append("======================\r\n");
         sb.append(recognisedConditionNames);
         sb.append("\r\n\r\n");
         sb.append("Known ICD codes:\r\n");
         sb.append("================\r\n");
         sb.append(recognisedICDCodes);

         return sb.toString();

     }

    //todo: scheduled task to refresh cache of SoPs from Repository
    // todo: scheduled task to update Repository from Legislation Register

}
