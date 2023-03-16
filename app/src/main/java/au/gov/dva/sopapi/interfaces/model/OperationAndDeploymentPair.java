package au.gov.dva.sopapi.interfaces.model;

public class OperationAndDeploymentPair {
    private OperationAndLegalSourcePair operationAndLegalSourcePair;
    private Deployment deployment;

    public OperationAndDeploymentPair(OperationAndLegalSourcePair operationAndLegalSourcePair, Deployment deployment) {
        this.operationAndLegalSourcePair = operationAndLegalSourcePair;
        this.deployment = deployment;
    }

    public OperationAndLegalSourcePair getOperationAndLegalSourcePair() {
        return operationAndLegalSourcePair;
    }

    public Deployment getDeployment() {
        return deployment;
    }
}
