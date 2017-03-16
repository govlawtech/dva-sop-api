package au.gov.dva.sopapi.client;

import javax.validation.constraints.NotNull;

import org.asynchttpclient.Realm;

public class SoPApiProxyClientNtlmSettings {
    private final String ipAddress;
    private final int port;
    private final String userName;
    private final String password;
    private final int secondsTimeOut;
    private final String ntlmDomain;
    private final String ntlmHost;

    public SoPApiProxyClientNtlmSettings(@NotNull String ipAddress, @NotNull int securedPort, @NotNull String userName, @NotNull String password, @NotNull int secondsTimeOut,
                                         @NotNull String ntlmHost, @NotNull String ntlmDomain) {
        this.ipAddress = ipAddress;
        this.port = securedPort;
        this.userName = userName;
        this.password = password;
        this.secondsTimeOut = secondsTimeOut;
        this.ntlmHost = ntlmHost;
        this.ntlmDomain = ntlmDomain;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getSecondsTimeOut() {
        return secondsTimeOut;
    }

    public String getNtlmHost() {
        return ntlmHost;
    }

    public String getNtlmDomain() {
        return ntlmDomain;
    }


}
