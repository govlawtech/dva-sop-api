package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import au.gov.dva.sopapi.exceptions.ActDeterminationServiceException;
import au.gov.dva.sopapi.interfaces.ActDeterminationServiceClient;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopsupport.vea.OperationJsonResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class RhPredicateFactory {

    private ActDeterminationServiceClient actDeterminationServiceClient;

    public RhPredicateFactory(ActDeterminationServiceClient actDeterminationServiceClient)
    {

        this.actDeterminationServiceClient = actDeterminationServiceClient;
    }


    public static Predicate<Deployment> createMrcaPredicate(String conditionName, ServiceDeterminationPair serviceDeterminationPair) {
        switch (conditionName) {
            case "posttraumatic stress disorder":
                return Operations.getMRCAIsWarlikePredicate(serviceDeterminationPair);
            case "anxiety disorder":
                return Operations.getMRCAIsWarlikePredicate(serviceDeterminationPair);
            case "adjustment disorder":
                return Operations.getMRCAIsWarlikePredicate(serviceDeterminationPair);
            default:
                return Operations.getMRCAIsOperationalPredicate(serviceDeterminationPair);
        }
    }

    private Predicate<Deployment> testDeploymentAgainstAds(Predicate<List<OperationJsonResponse>> whiteFilter) {

        return deployment -> {
            try {
                boolean result = actDeterminationServiceClient.matchesWhiteFilter(deployment.getOperationName(), whiteFilter).get(60, TimeUnit.SECONDS);
                return result;
            } catch (InterruptedException | ExecutionException | TimeoutException | DvaSopApiDtoRuntimeException e) {
                throw new ActDeterminationServiceException("Failed to get result from Acts Determination Service.", e);
            }
        };

    }

    private Predicate<List<OperationJsonResponse>> isWarlikeAccordingToAds = operationJsonResponses -> operationJsonResponses.stream()
            .anyMatch(operationJsonResponse ->
                    operationJsonResponse.isWarlike() &&
                            !operationJsonResponse.isMrcaWarlike() &&
                            !operationJsonResponse.isMrcaNonWarlike());


    private Predicate<List<OperationJsonResponse>> isOperationalAccordingToAds = operationJsonResponses -> operationJsonResponses.stream()
            .anyMatch(operationJsonResponse ->
                    operationJsonResponse.isOperational() ||
                            operationJsonResponse.isWarlike() ||
                            operationJsonResponse.isHazardous() ||
                            operationJsonResponse.isPeacekeeping() &&
                                    !operationJsonResponse.isMrcaNonWarlike() &&
                                    !operationJsonResponse.isMrcaWarlike());

    public Predicate<Deployment> createVeaPredicate(String conditionName) {

        switch (conditionName) {

            case "posttraumatic stress disorder":
                return testDeploymentAgainstAds(isWarlikeAccordingToAds);
            case "anxiety disorder":
                return testDeploymentAgainstAds(isWarlikeAccordingToAds);
            case "adjustment disorder":
                return testDeploymentAgainstAds(isWarlikeAccordingToAds);
            default:
                return testDeploymentAgainstAds(isOperationalAccordingToAds);
        }
    }
}
