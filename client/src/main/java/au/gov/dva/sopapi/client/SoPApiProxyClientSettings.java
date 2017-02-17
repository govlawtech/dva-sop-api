package au.gov.dva.sopapi.client;

import javax.validation.constraints.NotNull;

public class SoPApiProxyClientSettings {
    private final String ipAddress;
    private final int port;
    private final String userName;
    private final String password;
    private final int secondsTimeOut;

    public SoPApiProxyClientSettings(@NotNull String ipAddress, @NotNull int securedPort,@NotNull String userName,@NotNull String password,@NotNull int secondsTimeOut) {
        this.ipAddress = ipAddress;
        this.port = securedPort;
        this.userName = userName;
        this.password = password;
        this.secondsTimeOut = secondsTimeOut;
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
}
