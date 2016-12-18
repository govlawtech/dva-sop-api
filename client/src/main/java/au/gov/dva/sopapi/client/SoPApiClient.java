package au.gov.dva.sopapi.client;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoError;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.QueryParamLabels;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponseDto;
import au.gov.dva.sopapi.dtos.sopref.SoPRefDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Param;
import org.asynchttpclient.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SoPApiClient {

    private final URL baseUrl;
    private final String _service;
    private final String _serviceUrl;

    public SoPApiClient(URL baseUrl, String service)
    {
        this.baseUrl = baseUrl;
        this._service = service;
        this._serviceUrl = this.baseUrl + "/" + this._service;
    }

    public CompletableFuture<SoPRefDto> getFactors(String conditionName, String icdCodeVersion, String icdCodeValue, String incidentType, String standardOfProof)
    {
        List<Param> params = new ArrayList<>();
        params.add(new Param(QueryParamLabels.CONDITION_NAME, conditionName));
        params.add(new Param(QueryParamLabels.ICD_CODE_Value, icdCodeValue));
        params.add(new Param(QueryParamLabels.ICD_CODE_VERSION, icdCodeVersion));
        params.add(new Param(QueryParamLabels.INCIDENT_TYPE, incidentType));
        params.add(new Param(QueryParamLabels.STANDARD_OF_PROOF, standardOfProof));

        System.out.println(standardOfProof.toString());

        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        CompletableFuture<SoPRefDto> promise = asyncHttpClient
                .prepareGet(this._serviceUrl)
                .setHeader("Accept", "application/json")
                .addQueryParams(params)
                .execute()
                .toCompletableFuture()
                .thenApply(response -> response.getResponseBody())
                .thenApply(json -> SoPRefDto.fromJsonString(json.toString()));

        return promise;
    }

    public CompletableFuture<OperationsResponseDto> getOperations(LocalDate declaredAfter)
    {
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        CompletableFuture<OperationsResponseDto> promise = asyncHttpClient
                .prepareGet(this._serviceUrl)
                .setHeader("Accept", "application/json")
                .addQueryParam(QueryParamLabels.QUERY_DATE, declaredAfter.toString())
                .execute()
                .toCompletableFuture()
                .thenApply(response -> response.getResponseBody())
                .thenApply(json -> fromJsonString(json.toString()));

        return promise;
    }

    private static OperationsResponseDto fromJsonString(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        try {
            OperationsResponseDto operationsResponseDto =
                    objectMapper.readValue(json, OperationsResponseDto.class);
            return operationsResponseDto;
        } catch (IOException e) {
            throw new DvaSopApiDtoError(e);
        }
    }

    //todo: add method for SoP support once Nick sets up the return type DTOS.

}
