package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.ServiceDeterminationMockOperationLittenOnly;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import scala.math.Ordering;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class OperationNameMappingTests {

    ServiceDeterminationPair mockServiceDeterminationPair;

    public OperationNameMappingTests() throws IOException {
        mockServiceDeterminationPair = new ServiceDeterminationPair(
                TestUtils.getWarlikeDetermination(),
                TestUtils.getNonWarlikeDetermination());

    }

    @Test
    public void WorksWithOpPrefix() {
        Deployment testData = new Deployment() {

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
        ServiceDeterminationPair mock = new ServiceDeterminationPair(new ServiceDeterminationMockOperationLittenOnly(), new ServiceDeterminationMockOperationLittenOnly());
        Predicate<Deployment> underTest = Operations.getMRCAIsOperationalPredicate(mock);

        boolean result = underTest.test(testData);
        assert result;
    }


    @Test
    public void CompareIshOperations() {
        ImmutableList<String> ishOperationNames = ImmutableList.of(
                "OP ACCORDION",
                "OP MANITOU",
                "OP RAMP - NONWARLIKE LEBANON",
                "Operation ANODE",
                "OPERATION ARIKI (NZ)",
                "Operation ASLAN Zone A",
                "Operation ASLAN Zone B",
                "OPERATION ASTUTE",
                "OPERATION ATHENA (CANADA)",
                "OPERATION AUGURY",
                "Operation AZURE",
                "OPERATION BANNER",
                "Operation BASTILLE",
                "Operation BEL ISI II",
                "Operation BLAZER",
                "Operation BOLTON",
                "Operation Catalyst",
                "OPERATION CEDILLA",
                "Operation CHIRON",
                "Operation Citadel - East Timor",
                "Operation CORACLE",
                "Operation DAMASK X",
                "OPERATION DESERT STORM",
                "OPERATION ENDURING FREEDOM (US",
                "Operation FALCONER",
                "OPERATION GEMINI",
                "OPERATION GOODWILL",
                "OPERATION HABITAT",
                "OPERATION HAWICK (ZONE A)",
                "OPERATION HAWICK (ZONE B)",
                "Operation HEDGEROW",
                "OPERATION HERRICK (UK)",
                "Operation HIGHROAD",
                "Operation HUSKY",
                "Operation JOINT GUARDIAN",
                "Operation JURAL",
                "Operation KRUGER",
                "Operation MAZURKA",
                "Operation NORTHERN WATCH",
                "OPERATION OKRA ZONE A",
                "OPERATION OKRA ZONE B",
                "Operation OSIER",
                "Operation PALADIN",
                "Operation PALATE II",
                "Operation POLLARD",
                "Operation POMELO",
                "Operation PROVIDE COMFORT",
                "Operation QUICKSTEP",
                "Operation RIVERBANK",
                "Operation SLIPPER",
                "OPERATION SOLACE",
                "Operation SOUTHERN WATCH",
                "Operation SPIRE",
                "OPERATION TAMAR",
                "Operation TANAGER",
                "Operation TREK",
                "Operation UNTSO",
                "Operation VIGILANCE",
                "Operation WARDEN",
                "UNMCTT",
                "UNTAG"
        );

        Predicate<Deployment> underTest = Operations.getMRCAIsOperationalPredicate(mockServiceDeterminationPair);


        List<String> notMatched = new ArrayList<>();
        List<String> isMatched = new ArrayList<>();

        ishOperationNames.forEach(s -> {
            boolean matched = underTest.test(new Deployment() {
                @Override
                public String getOperationName() {
                    return s;
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
            });

            if (matched) isMatched.add(s);
                else notMatched.add(s);

        });

        System.out.println(String.format("MATCHED%n==========="));
        isMatched.stream().forEach(s -> System.out.println(s));


        System.out.println(String.format("NOT MATCHED%n=========="));
        notMatched.stream().forEach(s -> System.out.println(s));
    }
}

