package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.dtos.sopsupport.components.OperationTypeCode;
import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopsupport.processingrules.HasDateRangeImpl;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static Predicate<Deployment> getMRCAIsWarlikePredicate(ServiceDeterminationPair serviceDeterminationPair, Boolean validateDates, CaseTrace caseTrace) {
        ImmutableList<Operation> warlikeOperations = serviceDeterminationPair.getWarlike().getOperations();

        return getOperationalPredicateForMrca(warlikeOperations, validateDates, caseTrace,true);
    }


    public static Predicate<Deployment> getMRCAIsOperationalPredicate(Boolean validateDates, ServiceDeterminationPair serviceDeterminationPair, CaseTrace caseTrace) {


        ImmutableList<Operation> allOperations = ImmutableList.copyOf(Iterables.concat(
                serviceDeterminationPair.getWarlike().getOperations(),
                serviceDeterminationPair.getNonWarlike().getOperations()));

        return getOperationalPredicateForMrca(allOperations, validateDates, caseTrace,false);
    }


    private static boolean datesAreConsistent(Deployment deployment, Operation operation) {

        return DateTimeUtils.IsFirstOpenEndedIntervalWithinSecond(new HasDateRangeImpl(deployment.getStartDate(), deployment.getEndDate()), new HasDateRangeImpl(operation.getStartDate(), operation.getEndDate()));
    }


    private static Boolean deploymentMatchesOperation(Operation operation, Deployment deployment) {
        Boolean nameMatch = isNameMatch(deployment.getOperationName(), operation.getName());
        Boolean datesAreConsistent = datesAreConsistent(deployment, operation);
        return (nameMatch && datesAreConsistent);
    }

    private static ImmutableList<OperationAndLegalSourcePair> getMatchingOperations(ImmutableList<OperationAndLegalSourcePair> allOperations, Deployment deployment) {
        List<OperationAndLegalSourcePair> matching = allOperations.stream().filter(op -> deploymentMatchesOperation(op.get_operation(), deployment)).collect(Collectors.toList());
        return ImmutableList.copyOf(matching);
    }


    public static List<JustifiedMilitaryActivity> getMatchingOperationsForDeployments(ImmutableSet<ServiceDetermination> serviceDeterminations, List<Deployment> deployments) {
        ServiceDeterminationPair serviceDeterminationPair = getLatestDeterminationPair(serviceDeterminations);

        List<OperationAndLegalSourcePair> warlikeOps = serviceDeterminationPair.getWarlike().getOperations()
                .stream().map(o -> new OperationAndLegalSourcePair(serviceDeterminationPair.getWarlike().getCitation(), o))
                .collect(Collectors.toList());
        List<OperationAndLegalSourcePair> nonWarlikeOps = serviceDeterminationPair.getNonWarlike().getOperations()
                .stream().map(o -> new OperationAndLegalSourcePair(serviceDeterminationPair.getNonWarlike().getCitation(), o))
                .collect(Collectors.toList());

        ImmutableList<OperationAndLegalSourcePair> allOperations = ImmutableList.copyOf(Iterables.concat(
                warlikeOps,
                nonWarlikeOps));

        // get list of matching legal activities for each deployment
        Stream<DeploymentAndMatchingOperations> deploymentAndMatchingOperationsList = deployments.stream()
                .map(d -> new DeploymentAndMatchingOperations(d, getMatchingOperations(allOperations, d)));

        List<OperationAndDeploymentPair> operationAndDeploymentPairs = deploymentAndMatchingOperationsList
                .flatMap(deploymentToOperations -> deploymentToOperations.getOperations()
                        .stream().map(operation -> new OperationAndDeploymentPair(operation, deploymentToOperations.getDeployment())))
                .collect(Collectors.toList());

        Map<OperationAndLegalSourcePair, List<OperationAndDeploymentPair>> justifiedMilitaryActivitiesData =
                operationAndDeploymentPairs
                        .stream()
                        .collect(Collectors.groupingBy(o -> o.getOperationAndLegalSourcePair()));

        List<JustifiedMilitaryActivity> acc = new ArrayList<>();
        for (OperationAndLegalSourcePair operationAndLegalSourcePair : justifiedMilitaryActivitiesData.keySet()) {
            acc.add(new JustifiedMilitaryActivity(
                    new MilitaryActivity(
                            operationAndLegalSourcePair.get_operation().getName(),
                            operationAndLegalSourcePair.get_operation().getStartDate(),
                            operationAndLegalSourcePair.get_operation().getEndDate(),
                            operationAndLegalSourcePair.get_operation().getServiceType().toMilitaryOperationType(),
                            operationAndLegalSourcePair.get_legalSource()
                    ),
                    justifiedMilitaryActivitiesData
                            .get(operationAndLegalSourcePair)
                            .stream()
                            .map(i -> i.getDeployment())
                            .collect(Collectors.toList())
            ));
        }

        return acc;

    }

    private static ImmutableList<Tuple2<Pattern, String>> deploymentRegexToOpName = ImmutableList.of(
            Tuple2.apply(Pattern.compile("enduring\\s+freedom", Pattern.CASE_INSENSITIVE), "Enduring Freedom—Afghanistan"),
            Tuple2.apply(Pattern.compile("palate\\s*ii", Pattern.CASE_INSENSITIVE), "Palate II"),
            Tuple2.apply(Pattern.compile("palate$", Pattern.CASE_INSENSITIVE), "Palate"),
            Tuple2.apply(Pattern.compile("quickstep$", Pattern.CASE_INSENSITIVE), "Quickstep"),
            Tuple2.apply(Pattern.compile("quickstep\\s*tonga", Pattern.CASE_INSENSITIVE), "Quickstep Tonga")
    );

    public static Boolean isNameMatch(String serviceHistoryName, String officialOperationName) {

        // todo: move mappings to config

        Optional<Tuple2<Pattern, String>> applicableMatchingRule = deploymentRegexToOpName.stream()
                .filter(t -> t._1.matcher(serviceHistoryName).find())
                .findFirst();

        Boolean shouldUseRegexMatchingExclusively = applicableMatchingRule.isPresent();

        if (shouldUseRegexMatchingExclusively) {

            Boolean isMatch = applicableMatchingRule.get()._2.contentEquals(officialOperationName);
            return isMatch;
        } else {
            Boolean serviceHistoryNameContainsOfficialName = serviceHistoryName.toLowerCase().contains(officialOperationName.toLowerCase());
            return serviceHistoryNameContainsOfficialName;
        }
    }

    public static Boolean isOperationalAccordingToOpCodes(OperationTypeCode[] codes)
    {
        OperationTypeCode[] operationalCodes =
                new OperationTypeCode[] {
                        OperationTypeCode.WAR,
                        OperationTypeCode.NWAR,
                        OperationTypeCode.OPER,
                        OperationTypeCode.BNT,
                        OperationTypeCode.PEAK,
                        OperationTypeCode.HAZ
                    };

        OperationTypeCode[] matchedCodes = Arrays.stream(codes).filter(c -> Arrays.stream(operationalCodes).anyMatch(o -> o == c)).toArray(OperationTypeCode[]::new);
        return matchedCodes.length > 0;
    }

    public static Boolean isWarlikeAccordingToOpCodes(OperationTypeCode[] codes)
    {
        OperationTypeCode[] warlikeCodes =
                new OperationTypeCode[] {
                        OperationTypeCode.WAR
                    };

        OperationTypeCode[] matchedCodes = Arrays.stream(codes).filter(c -> Arrays.stream(warlikeCodes).anyMatch(o -> o == c)).toArray(OperationTypeCode[]::new);
        return matchedCodes.length > 0;
    }



    private static Predicate<Deployment> getOperationalPredicateForMrca(ImmutableList<Operation> allOperations, Boolean validateDates, CaseTrace caseTrace, Boolean warlikeOnly) {

        // even if validateDates flag is false, we want to validate the dates for operations matching name Okra, Paladin and Augury.
        // todo: move custom mappings to config

        return deployment -> {

            // defer to op codes to characterize deployment

            if (deployment instanceof CharacterisedDeployment && ((CharacterisedDeployment)deployment).getOperationTypeCodes().length > 0) {
                CharacterisedDeployment characterisedDeployment = (CharacterisedDeployment)deployment;
                if (warlikeOnly)
                {
                    return isWarlikeAccordingToOpCodes(characterisedDeployment.getOperationTypeCodes());
                }
                else
                {
                    return isOperationalAccordingToOpCodes(characterisedDeployment.getOperationTypeCodes());
                }
            }
            else {

                // one pass
                List<Operation> operationsWithMatchingName =
                        allOperations.stream().filter(o -> isNameMatch(deployment.getOperationName(), o.getName())).collect(Collectors.toList());

                ImmutableList<String> whiteListedOpsToValidate = ImmutableList.of("Augury", "Paladin", "Okra");

                Boolean deploymentShouldAlwaysBeValidated = whiteListedOpsToValidate.stream().anyMatch(op -> deployment.getOperationName().toLowerCase().contains(op.toLowerCase()));

                List<Operation> operationsWithMatchingNameAndDates =
                        operationsWithMatchingName.stream()
                                .filter(o -> DateTimeUtils.IsFirstOpenEndedIntervalWithinSecond(new HasDateRangeImpl(deployment.getStartDate(), deployment.getEndDate()), new HasDateRangeImpl(o.getStartDate(), o.getEndDate())))
                                .collect(Collectors.toList());

                if (!validateDates && !deploymentShouldAlwaysBeValidated) {
                    return !operationsWithMatchingName.isEmpty();
                }

                if (!operationsWithMatchingName.isEmpty() && operationsWithMatchingNameAndDates.isEmpty() && validateDates) {
                    StringBuilder logMessage = new StringBuilder();
                    logMessage.append("The dates of the deployment '" + deployment.getOperationName() + "' are not consistent with the dates of any matching known operations.");
                    logMessage.append(" The start date of the deployment is " + DateTimeFormatter.ISO_LOCAL_DATE.format(deployment.getStartDate()) + ".");
                    logMessage.append(deployment.getEndDate().isPresent() ? " The end date of the deployment is " + DateTimeFormatter.ISO_LOCAL_DATE.format(deployment.getEndDate().get()) + "." : " The deployment has no end date (ongoing).");
                    logMessage.append(" Candidate operations: ");
                    Function<Operation, String> prettyPrintOperation = op -> op.getName() + " (" + op.getServiceType().toString() + ")" + ": " + DateTimeFormatter.ISO_LOCAL_DATE.format(op.getStartDate()) + (op.getEndDate().isPresent() ? " to " + DateTimeFormatter.ISO_LOCAL_DATE.format(op.getEndDate().get()) : " to present");
                    String matchingOperationsString = String.join("; ", operationsWithMatchingName.stream().map(o -> prettyPrintOperation.apply(o)).collect(Collectors.toList()));
                    logMessage.append(matchingOperationsString);
                    caseTrace.addLoggingTrace(logMessage.toString());
                }

                return !operationsWithMatchingNameAndDates.isEmpty();
            }
        };
    }
}