package au.gov.dva.sopapi.client;

import au.gov.dva.sopapi.SharedConstants;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.QueryParamLabels;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponse;
import au.gov.dva.sopapi.dtos.sopref.SoPReferenceResponse;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import com.google.common.base.Charsets;
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
    private Optional<SoPApiProxyClientSettings> proxyConfig;
    private static volatile AsyncHttpClient asyncHttpClient;
    public SoPApiClient(URL baseUrl, Optional<SoPApiProxyClientSettings> proxyConfig)
    {
        this.baseUrl = baseUrl;
        this.proxyConfig = proxyConfig;
    }

    private AsyncHttpClient getOrCreateAsyncHttpClient(){
        if (asyncHttpClient == null)
        {
            asyncHttpClient = buildAsyncHttpClient(proxyConfig);
        }
        return asyncHttpClient;
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

    public CompletableFuture<SoPReferenceResponse> getFactorsForConditionName(String conditionName, IncidentType incidentType, StandardOfProof standardOfProof)
    {
        return getFactors(conditionName, null, null, incidentType,standardOfProof);
    }

    public CompletableFuture<SoPReferenceResponse> getFactorsForIcdCode(String icdCodeVersion, String icdCodeValue, IncidentType incidentType, StandardOfProof standardOfProof)
    {
        return getFactors(null, icdCodeVersion,icdCodeValue,incidentType,standardOfProof);
    }

    private CompletableFuture<SoPReferenceResponse> getFactors(String conditionName, String icdCodeVersion, String icdCodeValue, IncidentType incidentType, StandardOfProof standardOfProof)
    {

        URL serviceUrl = getServiceUrl(baseUrl, SharedConstants.Routes.GET_SOPFACTORS);
        List<Param> params = new ArrayList<>();
        params.add(new Param(QueryParamLabels.CONDITION_NAME, conditionName));
        params.add(new Param(QueryParamLabels.ICD_CODE_VALUE, icdCodeValue));
        params.add(new Param(QueryParamLabels.ICD_CODE_VERSION, icdCodeVersion));
        params.add(new Param(QueryParamLabels.INCIDENT_TYPE, incidentType.toString()));
        params.add(new Param(QueryParamLabels.STANDARD_OF_PROOF, standardOfProof.toAbbreviatedString()));

        CompletableFuture<SoPReferenceResponse> promise = getOrCreateAsyncHttpClient()
                .prepareGet(serviceUrl.toString())
                .setHeader("Accept", "application/json; charset=utf-8")
                .setHeader("Content-Type","application/json; charset=utf-8")
                .addQueryParams(params)
                .execute()
                .toCompletableFuture()
                .thenApply(response -> {
                    if (response.getStatusCode() == 200) {
                        return SoPReferenceResponse.fromJsonString(response.getResponseBody());
                    } else {
                        throw new SoPApiClientError(response.getResponseBody(Charsets.UTF_8));
                    }
                });

        return promise;
    }

    public CompletableFuture<OperationsResponse> getOperations()
    {
        URL serviceUrl = getServiceUrl(baseUrl, SharedConstants.Routes.GET_OPERATIONS);
        CompletableFuture<OperationsResponse> promise = getOrCreateAsyncHttpClient()
                .prepareGet(serviceUrl.toString())
                .setHeader("Accept", "application/json; charset=utf-8")
                .setHeader("Content-Type","application/json; charset=utf-8")
                .execute()
                .toCompletableFuture()
                .thenApply(response -> {
                    if (response.getStatusCode() == 200)
                        return OperationsResponse.fromJsonString(response.getResponseBody(Charsets.UTF_8));
                    else throw new SoPApiClientError(response.getResponseBody(Charsets.UTF_8));
                });

        return promise;
    }

    public CompletableFuture<SopSupportResponseDto> getSatisfiedFactors(String jsonRequestBody) {
        URL serviceUrl = getServiceUrl(baseUrl, SharedConstants.Routes.GET_SERVICE_CONNECTION);

        CompletableFuture<SopSupportResponseDto> promise = getOrCreateAsyncHttpClient()
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
