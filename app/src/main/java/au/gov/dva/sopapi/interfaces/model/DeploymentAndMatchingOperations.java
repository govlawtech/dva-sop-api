package au.gov.dva.sopapi.interfaces.model;


import com.google.common.collect.ImmutableList;

public class DeploymentAndMatchingOperations {
    private Deployment Deployment;
    private ImmutableList<OperationAndLegalSourcePair> Operations;

    public DeploymentAndMatchingOperations(au.gov.dva.sopapi.interfaces.model.Deployment deployment, ImmutableList<OperationAndLegalSourcePair> operations) {
        Deployment = deployment;
        Operations = operations;
    }

    public au.gov.dva.sopapi.interfaces.model.Deployment getDeployment() {
        return Deployment;
    }

    public ImmutableList<OperationAndLegalSourcePair> getOperations() {
        return Operations;
    }
}
