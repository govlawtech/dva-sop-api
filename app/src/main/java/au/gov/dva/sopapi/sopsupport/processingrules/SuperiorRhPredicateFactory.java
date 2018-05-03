package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.exceptions.ActDeterminationServiceException;
import au.gov.dva.sopapi.interfaces.ActDeterminationServiceClient;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopsupport.vea.ServiceRegion;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class SuperiorRhPredicateFactory implements  IRhPredicateFactory {

    private ServiceDeterminationPair serviceDeterminationPair;

    public SuperiorRhPredicateFactory(ServiceDeterminationPair serviceDeterminationPair)
    {
        this.serviceDeterminationPair = serviceDeterminationPair;
    }

    @Override
    public Predicate<Deployment> createMrcaOrVeaPredicate(ConditionDto conditionDto)
    {
        if (conditionDto.get_incidentDateRangeDto().get_startDate().isAfter(LocalDate.of(2004, 06, 30))) {
            return createMrcaPredicate(conditionDto.get_conditionName());
        }
        else {
            return createVeaPredicate(conditionDto.get_conditionName());
        }
    }

    @Override
    public Predicate<Deployment> createMrcaPredicate(String conditionName) {
        switch (conditionName.toLowerCase()) {
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


    @Override
    public Predicate<Deployment> createVeaPredicate(String conditionName) {

        switch (conditionName.toLowerCase()) {

            case "posttraumatic stress disorder":
   //             return testDeploymentAgainstAds(isWarlikeAccordingToAds);
            case "anxiety disorder":
     //           return testDeploymentAgainstAds(isWarlikeAccordingToAds);
            case "adjustment disorder":
       //         return testDeploymentAgainstAds(isWarlikeAccordingToAds);
            default:
         //       return testDeploymentAgainstAds(isRhServiceAccordingToADS);
        }
        return null;
    }

}
