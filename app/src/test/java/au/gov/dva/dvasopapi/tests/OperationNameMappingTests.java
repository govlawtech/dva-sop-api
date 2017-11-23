package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.ServiceDeterminationMockOperationLittenOnly;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;

public class OperationNameMappingTests {


    @Test
    public void WorksWithOpPrefix() {
        Deployment testData = new Deployment()  {

            @Override
            public String getOperationName() {
                return "Op litten";
            }

            @Override
            public String getEvent() {
                return null;
            }

            @Override
            public LocalDate getStartDate() {
                return null;
            }

            @Override
            public Optional<LocalDate> getEndDate() {
                return null;
            }
        };
        ServiceDeterminationPair mock = new ServiceDeterminationPair(new ServiceDeterminationMockOperationLittenOnly(),new ServiceDeterminationMockOperationLittenOnly());
        Predicate<Deployment> underTest = ProcessingRuleFunctions.getMRCAIsOperationalPredicate(mock);

        boolean result = underTest.test(testData);
        assert result;
    }
}
