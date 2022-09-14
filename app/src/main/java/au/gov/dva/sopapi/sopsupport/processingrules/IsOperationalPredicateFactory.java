package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Deployment;

import java.util.function.Predicate;

public interface IsOperationalPredicateFactory {
    Predicate<Deployment> createMrcaOrVeaPredicate(Condition condition, Boolean validateDates, CaseTrace caseTrace);

}
