package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.veaops.Facade;
import au.gov.dva.sopapi.veaops.interfaces.VeaOperationalServiceRepository;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.util.function.Predicate;

public class PredicateFactory implements IsOperationalPredicateFactory {

    private ServiceDeterminationPair serviceDeterminationPair;
    private final VeaOperationalServiceRepository veaRepo;

    ImmutableSet<String> namesOfMentalConditionsWhereWarlikeServiceIsRequired = ImmutableSet.of(
            "posttraumatic stress disorder",
            "anxiety disorder",
            "adjustment disorder"
    );

    public PredicateFactory(ServiceDeterminationPair serviceDeterminationPair, VeaOperationalServiceRepository veaRepo) {
        this.serviceDeterminationPair = serviceDeterminationPair;
        this.veaRepo = veaRepo;
    }

    @Override
    public Predicate<Deployment> createMrcaOrVeaPredicate(Condition condition, Boolean validateDates, CaseTrace caseTrace) {
        if (condition.getStartDate().isAfter(LocalDate.of(2004, 06, 30))) {
            return createMrcaPredicate(condition.getSopPair().getConditionName(), validateDates, caseTrace);
        } else {
            return createVeaPredicate(condition.getSopPair().getConditionName(),validateDates);
        }
    }

    public Predicate<Deployment> createMrcaPredicate(String conditionName, Boolean validateDates, CaseTrace caseTrace) {
        if (namesOfMentalConditionsWhereWarlikeServiceIsRequired.contains(conditionName.toLowerCase())) {
            return Operations.getMRCAIsWarlikePredicate(serviceDeterminationPair, validateDates, caseTrace);
        } else {
            return Operations.getMRCAIsOperationalPredicate(validateDates, serviceDeterminationPair, caseTrace);
        }
    }


    public Predicate<Deployment> createVeaPredicate(String conditionName, Boolean validateDates) {

        if (namesOfMentalConditionsWhereWarlikeServiceIsRequired.contains(conditionName.toLowerCase()))
        {
            return buildIsWarlikeVeaPredicate(validateDates);
        }
        else {
            return buildIsOperationalVeaPredicate(validateDates);
        }
    }

    private Predicate<Deployment> buildIsOperationalVeaPredicate(Boolean validateDates) {
        return deployment -> Facade.isOperational(deployment.getOperationName(), deployment.getStartDate(), deployment.getEndDate(), validateDates, veaRepo);
    }

    private Predicate<Deployment> buildIsWarlikeVeaPredicate(Boolean validateDates) {
        return deployment -> Facade.isWarlike(deployment.getOperationName(), deployment.getStartDate(), deployment.getEndDate(), validateDates, veaRepo);
    }

}
