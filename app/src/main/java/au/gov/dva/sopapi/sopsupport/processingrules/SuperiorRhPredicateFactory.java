package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.veaops.Facade;
import au.gov.dva.sopapi.veaops.interfaces.VeaOperationalServiceRepository;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.util.function.Predicate;

public class SuperiorRhPredicateFactory implements IRhPredicateFactory {

    private ServiceDeterminationPair serviceDeterminationPair;
    private final VeaOperationalServiceRepository veaRepo;

    ImmutableSet<String> namesOfMentalConditionsWhereWarlikeServiceIsRequired = ImmutableSet.of(
            "posttraumatic stress disorder",
            "anxiety disorder",
            "adjustment disorder"
    );

    public SuperiorRhPredicateFactory(ServiceDeterminationPair serviceDeterminationPair, VeaOperationalServiceRepository veaRepo) {
        this.serviceDeterminationPair = serviceDeterminationPair;
        this.veaRepo = veaRepo;
    }

    @Override
    public Predicate<Deployment> createMrcaOrVeaPredicate(ConditionDto conditionDto) {
        if (conditionDto.get_incidentDateRangeDto().get_startDate().isAfter(LocalDate.of(2004, 06, 30))) {
            return createMrcaPredicate(conditionDto.get_conditionName());
        } else {
            return createVeaPredicate(conditionDto.get_conditionName());
        }
    }



    @Override
    public Predicate<Deployment> createMrcaPredicate(String conditionName) {
        if (namesOfMentalConditionsWhereWarlikeServiceIsRequired.contains(conditionName.toLowerCase())) {
            return Operations.getMRCAIsWarlikePredicate(serviceDeterminationPair);
        } else {
            return Operations.getMRCAIsOperationalPredicate(serviceDeterminationPair);
        }
    }


    @Override
    public Predicate<Deployment> createVeaPredicate(String conditionName) {

        if (namesOfMentalConditionsWhereWarlikeServiceIsRequired.contains(conditionName.toLowerCase()))
        {
            return buildIsWarlikeVeaPredicate();
        }
        else {
            return buildIsOperationalVeaPredicate();
        }
    }

    private Predicate<Deployment> buildIsOperationalVeaPredicate() {
        return deployment -> Facade.isOperational(deployment.getOperationName(), deployment.getStartDate(), deployment.getEndDate(), veaRepo);
    }

    private Predicate<Deployment> buildIsWarlikeVeaPredicate() {
        return deployment -> Facade.isWarlike(deployment.getOperationName(), deployment.getStartDate(), deployment.getEndDate(), veaRepo);
    }

}
