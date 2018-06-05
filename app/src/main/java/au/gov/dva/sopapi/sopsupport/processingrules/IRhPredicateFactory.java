package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.interfaces.model.Deployment;

import java.util.function.Predicate;

/**
 * Created by nick on 12/7/2017.
 */
public interface IRhPredicateFactory {
    Predicate<Deployment> createMrcaOrVeaPredicate(ConditionDto conditionDto);

    Predicate<Deployment> createMrcaPredicate(String conditionName);

    Predicate<Deployment> createVeaPredicate(String conditionName);
}
