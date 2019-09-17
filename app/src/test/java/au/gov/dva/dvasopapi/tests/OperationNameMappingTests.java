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
    public void PaladinNotMatchedBecauseOfIncorrectDates() {
        // actual Paladin warlike operation runs 12 July 2006—14 August 2006
        // actual non-warlike operation runs 21 April 2003–11 July 2006

        Deployment mockDeployment = new Deployment() {
            @Override
            public String getOperationName() {
                return "operation Paladin";
            }

            @Override
            public String getEvent() {
                return null;
            }

            // one day before the actual start date
            @Override
            public LocalDate getStartDate() {
                return LocalDate.of(2006, 7, 11);
            }

            // ends on last date of operation
            @Override
            public Optional<LocalDate> getEndDate() {
                return Optional.of(LocalDate.of(2006, 8, 14));
            }
        };

        Predicate<Deployment> underTest = Operations.getMRCAIsWarlikePredicate(mockServiceDeterminationPair);
        boolean result = underTest.test(mockDeployment);
        assert (result == false);

    }

    @Test
    public void PaladinNotMatchedBecauseNonWarlike() {


        Deployment mockDeployment = new Deployment() {
            @Override
            public String getOperationName() {
                return "OPERATION PALADIN";
            }

            @Override
            public String getEvent() {
                return null;
            }

            // start of non-warlike paladin - correct dates
            @Override
            public LocalDate getStartDate() {
                return LocalDate.of(2003, 4, 21);
            }

            // end of non-warlike palidin - correct dates
            @Override
            public Optional<LocalDate> getEndDate() {
                return Optional.of(LocalDate.of(2006, 7, 11));
            }
        };

        Predicate<Deployment> underTest = Operations.getMRCAIsWarlikePredicate(mockServiceDeterminationPair);
        boolean result = underTest.test(mockDeployment);
        assert (result == false);

    }

    @Test
    public void PaladinMatchedBecauseWarlike() {

        Deployment mockDeployment = new Deployment() {
            @Override
            public String getOperationName() {
                return "OPERATION PALADIN";
            }

            @Override
            public String getEvent() {
                return null;
            }

            // start of warlike paladin - correct dates
            @Override
            public LocalDate getStartDate() {
                return LocalDate.of(2006, 7, 12);
            }

            // end of warlike palidin - correct dates
            @Override
            public Optional<LocalDate> getEndDate() {
                return Optional.of(LocalDate.of(2006, 8, 14));
            }
        };

        Predicate<Deployment> underTest = Operations.getMRCAIsWarlikePredicate(mockServiceDeterminationPair);
        boolean result = underTest.test(mockDeployment);
        assert (result == true);

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
                "International Security Assistance Force",
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
                "OPERATION ENDURING FREEDOM",
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
                    return LocalDate.of(2000, 1, 1);
                }

                @Override
                public Optional<LocalDate> getEndDate() {
                    return Optional.empty();
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

    @Test
    public void AuguryWithCorrectDatesShouldMatchForMRCA() {
        Deployment mockDeployment = new Deployment() {
            @Override
            public String getOperationName() {
                return "OPERATION AUGURY";
            }

            @Override
            public String getEvent() {
                return null;
            }

            // WARLIKE AND NON-warlike Augury start on 2016-04-28 for MRCA
            @Override
            public LocalDate getStartDate() {
                return LocalDate.of(2016, 4, 28);
            }

            @Override
            public Optional<LocalDate> getEndDate() {
                return Optional.empty();
            }
        };

        Predicate<Deployment> underTest = Operations.getMRCAIsWarlikePredicate(mockServiceDeterminationPair);
        boolean result = underTest.test(mockDeployment);
        assert (result == true);

    }

    @Test
    public void OkraShouldMatchIfWithinDates() {
        Deployment mockDeployment = new Deployment() {
            @Override
            public String getOperationName() {
                return "OPERATION OKRA ZONE A";
            }

            @Override
            public String getEvent() {
                return null;
            }

            // These are the actual Okra operations:
            //  StoredOperation{name='Okra', startDate=2014-08-09, endDate=Optional[2015-09-08], serviceType=warlike}
            //  StoredOperation{name='Okra', startDate=2015-09-09, endDate=Optional.empty, serviceType=warlike}
            //  StoredOperation{name='Okra', startDate=2014-07-01, endDate=Optional[2014-08-08], serviceType=non-warlike}
            //  StoredOperation{name='Okra', startDate=2014-08-09, endDate=Optional[2015-09-08], serviceType=non-warlike}
            //  StoredOperation{name='Okra', startDate=2015-09-09, endDate=Optional.empty, serviceType=non-warlike}


            // should match second row
            @Override
            public LocalDate getStartDate() {
                return LocalDate.of(2015,9,9);
            }

            @Override
            public Optional<LocalDate> getEndDate() {
                return Optional.empty();
            }
        };

        Predicate<Deployment> underTest = Operations.getMRCAIsWarlikePredicate(mockServiceDeterminationPair);
        boolean result = underTest.test(mockDeployment);
        assert (result == true);
    }
}

