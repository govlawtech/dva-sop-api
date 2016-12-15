package au.gov.dva.sopapi.client;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponseDto;
import au.gov.dva.sopapi.dtos.sopref.SoPRefDto;

import java.net.URL;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public class SoPApiClient {

    private final URL baseUrl;

    public SoPApiClient(URL baseUrl)
    {

        this.baseUrl = baseUrl;
    }

    public CompletableFuture<SoPRefDto> getFactors(String conditionName, String icdCodeVersion, String icdCodeValue, IncidentType incidentType, StandardOfProof standardOfProof)
    {
        // todo:
        // - make async call to getSopFactors endpoint (need to set headers, look at Application.java to see what is expected)
        // - deserialize the response to SoPRefDto using Jackson



        // this is just here to stop Findbugs complaining - can remove later
        System.out.print(baseUrl);


        return null;
    }

    public CompletableFuture<OperationsResponseDto> getOperations(LocalDate declaredAfter)
    {
        return null;
    }


    //todo: add method for SoP support once Nick sets up the return type DTOS.

}
