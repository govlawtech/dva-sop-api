package au.gov.dva.sopapi.client;

import au.gov.dva.sopapi.SharedConstants;
import au.gov.dva.sopapi.dtos.QueryParamLabels;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponseDto;
import au.gov.dva.sopapi.dtos.sopref.SoPRefDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import org.asynchttpclient.*;
import org.asynchttpclient.proxy.ProxyServer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SoPApiClient {

    private final URL baseUrl;
    private final AsyncHttpClient asyncHttpClient;


    public SoPApiClient(URL baseUrl, Optional<SoPApiProxyClientSettings> proxyConfig)
    {
        this.baseUrl = baseUrl;
        asyncHttpClient = buildAsyncHttpClient(proxyConfig);
    }

    private AsyncHttpClient buildAsyncHttpClient(Optional<SoPApiProxyClientSettings> soPApiProxyClientSettings)
    {
        if (soPApiProxyClientSettings.isPresent()) {
            SoPApiProxyClientSettings proxyClientSettings = soPApiProxyClientSettings.get();

            Realm realm = new Realm.Builder(proxyClientSettings.getUserName(), proxyClientSettings.getPassword())
                    .setScheme(Realm.AuthScheme.BASIC)
                    .build();

            ProxyServer proxyServer = new ProxyServer.Builder(proxyClientSettings.getIpAddress(), proxyClientSettings.getPort())
                    .setRealm(realm)
                    .setSecuredPort(proxyClientSettings.getPort())
                    .build();

            AsyncHttpClientConfig cf = new DefaultAsyncHttpClientConfig.Builder()
                    .setProxyServer(proxyServer)
                    .setAcceptAnyCertificate(true)
                    .setConnectTimeout(proxyClientSettings.getSecondsTimeOut())
                    .build();

            return new DefaultAsyncHttpClient(cf);
        }
        else {
            return new DefaultAsyncHttpClient();
        }
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

        CompletableFuture<SopSupportResponseDto> promise = asyncHttpClient
                .preparePost(serviceUrl.toString())
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
