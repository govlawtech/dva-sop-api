package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.ServiceType;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Operations {

    public static Optional<ServiceDetermination> getLatestServiceDetermination(ImmutableSet<ServiceDetermination> allServiceDeterminations, ServiceType serviceType) {
        List<OffsetDateTime> commencementDates = allServiceDeterminations.stream().map(sd -> sd.getCommencementDate()).collect(Collectors.toList());

        assert (commencementDates.size() == commencementDates.stream().distinct().count());

        return allServiceDeterminations.stream()
                .filter(sd -> sd.getServiceType().equals(serviceType))
                .sorted((o1, o2) -> o2.getCommencementDate().compareTo(o1.getCommencementDate()))
                .findFirst();
    }


    public static ServiceDeterminationPair getLatestDeterminationPair(ImmutableSet<ServiceDetermination> allServiceDeterminations) {
        Optional<ServiceDetermination> relevantWarlikeDetermination = Operations.getLatestServiceDetermination(allServiceDeterminations, ServiceType.WARLIKE);

        Optional<ServiceDetermination> relevantNonWarlikeDetermination = Operations.getLatestServiceDetermination(allServiceDeterminations, ServiceType.NON_WARLIKE);

        if (!relevantNonWarlikeDetermination.isPresent() || !relevantWarlikeDetermination.isPresent()) {
            throw new DvaSopApiRuntimeException("Missing service determinations.");
        }

        return new ServiceDeterminationPair(relevantWarlikeDetermination.get(), relevantNonWarlikeDetermination.get());

    }

    public static Predicate<Deployment> getMRCAIsOperationalPredicate(ServiceDeterminationPair serviceDeterminationPair) {
        ImmutableList<Operation> allOperations = ImmutableList.copyOf(Iterables.concat(
                serviceDeterminationPair.getWarlike().getOperations(),
                serviceDeterminationPair.getNonWarlike().getOperations()));

        ImmutableSet<String> opNames = allOperations.stream()
                .map(operation -> operation.getName())
                .map(name -> name.toLowerCase())
                .distinct()
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));

        ImmutableSet<String> specialWhitelist = ImmutableSet.of(
                "operation enduring freedom (us"); // note omitted final paren is intentional

        Sets.SetView<String> allToMatch = Sets.union(opNames, specialWhitelist);

        return (deployment ->
                allToMatch.stream()
                        .anyMatch(s -> deployment
                                .getOperationName()
                                .toLowerCase()
                                .contains(s)));
    }


}
