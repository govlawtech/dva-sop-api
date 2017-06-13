package au.gov.dva.sopapi.client;

public class SoPApiClientError extends RuntimeException {
    public SoPApiClientError(String msg) {
        super(msg);
    }
}
