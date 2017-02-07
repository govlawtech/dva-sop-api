package au.gov.dva.sopapi.client;

import au.gov.dva.sopapi.SharedConstants;
import au.gov.dva.sopapi.dtos.QueryParamLabels;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponseDto;
import au.gov.dva.sopapi.dtos.sopref.SoPRefDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Param;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SoPApiClient {

    // todo : error handling
    private final URL baseUrl;

    public SoPApiClient(URL baseUrl)
    {
        this.baseUrl = baseUrl;
    }


    private static URL getServiceUrl(URL baseUrl, String serviceRouteWithLeadingSlash) {
        try {
            assert(!baseUrl.toString().endsWith("/"));
            return URI.create(baseUrl + serviceRouteWithLeadingSlash).toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public CompletableFuture<SoPRefDto> getFactors(String conditionName, String icdCodeVersion, String icdCodeValue, String incidentType, String standardOfProof)
    {

        URL serviceUrl = getServiceUrl(baseUrl, SharedConstants.Routes.GET_SOPFACTORS);
        List<Param> params = new ArrayList<>();
        params.add(new Param(QueryParamLabels.CONDITION_NAME, conditionName));
        params.add(new Param(QueryParamLabels.ICD_CODE_VALUE, icdCodeValue));
        params.add(new Param(QueryParamLabels.ICD_CODE_VERSION, icdCodeVersion));
        params.add(new Param(QueryParamLabels.INCIDENT_TYPE, incidentType));
        params.add(new Param(QueryParamLabels.STANDARD_OF_PROOF, standardOfProof));

        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        CompletableFuture<SoPRefDto> promise = asyncHttpClient
                .prepareGet(serviceUrl.toString())
                .setHeader("Accept", "application/json; charset=utf-8")
                .setHeader("Content-Type","application/json; charset=utf-8")
                .addQueryParams(params)
                .execute()
                .toCompletableFuture()
                .thenApply(response -> response.getResponseBody())
                .thenApply(json -> SoPRefDto.fromJsonString(json.toString()));

        return promise;
    }

    public CompletableFuture<OperationsResponseDto> getOperations()
    {
        URL serviceUrl = getServiceUrl(baseUrl, SharedConstants.Routes.GET_OPERATIONS);
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        CompletableFuture<OperationsResponseDto> promise = asyncHttpClient
                .prepareGet(serviceUrl.toString())
                .setHeader("Accept", "application/json; charset=utf-8")
                .setHeader("Content-Type","application/json; charset=utf-8")
                .execute()
                .toCompletableFuture()
                .thenApply(response -> response.getResponseBody())
                .thenApply(json -> OperationsResponseDto.fromJsonString(json.toString()));

        return promise;
    }

    public CompletableFuture<SopSupportResponseDto> getSatisfiedFactors(String jsonRequestBody) {
        URL serviceUrl = getServiceUrl(baseUrl, SharedConstants.Routes.GET_SERVICE_CONNECTION);

        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        CompletableFuture<SopSupportResponseDto> promise = asyncHttpClient
                .prepareGet(serviceUrl.toString())
                .setHeader("Accept", "application/json; charset=utf-8")
                .setHeader("Content-Type","application/json; charset=utf-8")
                .setBody(jsonRequestBody)
                .execute()
                .toCompletableFuture()
                .thenApply(response -> {
                            if (response.getStatusCode() == 200) {

                                return response.getResponseBody();

                            }
                            else {
                                throw new SoPApiClientError(buildErrorMsg(response.getStatusCode(),response.getResponseBody()));
                            }
                        }
                    )

                .thenApply(json -> SopSupportResponseDto.fromJsonString(json.toString()));

        return promise;
    }

    private static String buildErrorMsg(Integer statusCode, String msg)
    {
        return String.format("HTTP Status Code: %d, %s.", statusCode, msg);
    }

}
