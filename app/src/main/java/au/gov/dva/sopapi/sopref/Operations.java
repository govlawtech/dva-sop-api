package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.dtos.MilitaryActivity;
import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Operations {

    private static Logger logger = LoggerFactory.getLogger("dvasopapi.processingrules");

    public static Optional<ServiceDetermination> getLatestServiceDetermination(ImmutableSet<ServiceDetermination> allServiceDeterminations, ServiceType serviceType) {
        List<OffsetDateTime> commencementDates = allServiceDeterminations.stream().map(sd -> sd.getCommencementDate()).collect(Collectors.toList());

        //assert (commencementDates.size() == commencementDates.stream().distinct().count());

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


    public static Predicate<Deployment> getMRCAIsWarlikePredicate(ServiceDeterminationPair serviceDeterminationPair) {
        ImmutableList<Operation> warlikeOperations = serviceDeterminationPair.getWarlike().getOperations();
        return getPredicateForMrcaOperations(warlikeOperations);

    }


    public static Predicate<Deployment> getMRCAIsOperationalPredicate(ServiceDeterminationPair serviceDeterminationPair) {
        ImmutableList<Operation> allOperations = ImmutableList.copyOf(Iterables.concat(
                serviceDeterminationPair.getWarlike().getOperations(),
                serviceDeterminationPair.getNonWarlike().getOperations()));

        return getPredicateForMrcaOperations(allOperations);
    }


    private static boolean datesAreConsistent(Deployment deployment, Operation operation)
    {
        if (deployment.getStartDate().isBefore(operation.getStartDate()))
        {
            return false;
        }

        if (!deployment.getEndDate().isPresent() && operation.getEndDate().isPresent())
        {
            return false;
        }

        if (deployment.getEndDate().isPresent() && operation.getEndDate().isPresent())
        {
            if (deployment.getEndDate().get().isAfter(operation.getEndDate().get()))
            {
                return false;
            }
        }

        return true;
    }


    private static boolean operationWithConsistentDatesExist(Deployment deployment, ImmutableList<Operation> operations)
    {
        boolean matchFound = operations.stream().anyMatch(operation -> datesAreConsistent(deployment,operation));
        if (!matchFound)
        {
            StringBuilder warning = new StringBuilder();
            warning.append(String.format("Deployment dates not consistent with matching operations.%n"));
            warning.append(String.format("DEPLOYMENT:%n"));
            warning.append(String.format("%s%n",deployment.toString()));
            warning.append(String.format("OPERATIONS:%n"));
            operations.forEach(operation -> warning.append(String.format("%s%n",operation.toString())));
            logger.warn(warning.toString());

            return false;
        }
        return true;
    }

    private static Boolean deploymentMatchesOperation(Operation operation, Deployment deployment) {
        Boolean deploymentNameToLowerContainsOpName = deployment.getOperationName().toLowerCase().contains(operation.getName().toLowerCase());
        Boolean datesAreConsistent = datesAreConsistent(deployment,operation);
        return (deploymentNameToLowerContainsOpName && datesAreConsistent);
    }

    private static List<OperationAndLegalSourcePair> getMatchingOperations(ImmutableList<OperationAndLegalSourcePair> allOperations, Deployment deployment)
    {
        List<OperationAndLegalSourcePair> matching = allOperations.stream().filter(op -> deploymentMatchesOperation(op.get_operation(),deployment)).collect(Collectors.toList());
        return matching;
    }


    public static List<OperationAndLegalSourcePair> getMatchingOperationsForDeployments(ImmutableSet<ServiceDetermination> serviceDeterminations, List<Deployment> deployments)
    {
        ServiceDeterminationPair serviceDeterminationPair = getLatestDeterminationPair(serviceDeterminations);

        List<OperationAndLegalSourcePair> warlikeOps = serviceDeterminationPair.getWarlike().getOperations()
                .stream().map(o -> new OperationAndLegalSourcePair(serviceDeterminationPair.getWarlike().getCitation(), o))
                .collect(Collectors.toList());
        List<OperationAndLegalSourcePair> nonWarlikeOps = serviceDeterminationPair.getNonWarlike().getOperations()
                .stream().map(o -> new OperationAndLegalSourcePair(serviceDeterminationPair.getNonWarlike().getCitation(),o))
                .collect(Collectors.toList());


        ImmutableList<OperationAndLegalSourcePair> allOperations =  ImmutableList.copyOf(Iterables.concat(
                warlikeOps,
                nonWarlikeOps));


        List<OperationAndLegalSourcePair> acc = new ArrayList<>();
        for (Deployment d : deployments)
        {
            List<OperationAndLegalSourcePair> matching = getMatchingOperations(allOperations,d);
            if (!matching.isEmpty())
                acc.addAll(matching);
        }
        return acc;

    }


    private static Predicate<Deployment> getPredicateForMrcaOperations(ImmutableList<Operation> allOperations) {

        ImmutableSet<String> opNames = allOperations.stream()
                .map(operation -> operation.getName())
                .map(name -> name.toLowerCase())
                .distinct()
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));

        ImmutableSet<String> specialWhitelist = ImmutableSet.of(
                "enduring freedom");

        ImmutableList<String> mrcaOpertionNamesForWhichToValidateDates = ImmutableList.of(
                "Paladin",
                "Okra",
                "Augury"
        );

        Sets.SetView<String> allToMatch = Sets.union(opNames, specialWhitelist);



        return (deployment -> {

            boolean nameMatches =  allToMatch.stream()
                        .anyMatch(s -> deployment
                                .getOperationName()
                                .toLowerCase()
                                .contains(s));

            if (!nameMatches)
            {
                return false;
            }

//            boolean dateMatchRequired = mrcaOpertionNamesForWhichToValidateDates
//                    .stream()
//                    .map(s -> s.toLowerCase())
//                    .anyMatch(s -> deployment.getOperationName().toLowerCase().contains(s));
//
//            if (dateMatchRequired)
//            {
                ImmutableList<Operation> operationsWithSameName = allOperations.stream()
                        .filter(operation -> deployment.getOperationName().toLowerCase().contains(operation.getName().toLowerCase())).collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));

                if (operationWithConsistentDatesExist(deployment,operationsWithSameName))
                {
                    return true;
                }
                else {
                    return false;
                }
//            }
  //          return true;
        });
    }

}
