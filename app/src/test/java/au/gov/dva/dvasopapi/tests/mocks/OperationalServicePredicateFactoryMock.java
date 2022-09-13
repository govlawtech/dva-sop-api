package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.sopsupport.processingrules.IsOperationalPredicateFactory;

import java.util.function.Predicate;

public class OperationalServicePredicateFactoryMock implements IsOperationalPredicateFactory {
    @Override
    public Predicate<Deployment> createMrcaOrVeaPredicate(ConditionDto conditionDto, Boolean validateDates, CaseTrace caseTrace) {
        Predicate<Deployment> isOperationalMock = deployment -> deployment.getOperationName() == "OPERATIONAL";
        return isOperationalMock;
    }
}
