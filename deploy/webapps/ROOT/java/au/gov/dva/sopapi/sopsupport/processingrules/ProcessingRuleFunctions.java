package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.exceptions.ProcessingRuleError;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProcessingRuleFunctions {

    private static Logger logger = LoggerFactory.getLogger(ProcessingRuleFunctions.class.getSimpleName());

    public static Optional<Service> identifyServiceDuringOrAfterWhichConditionOccurs(ImmutableSet<Service> services, OffsetDateTime conditionStartDate) {


        logger.trace("Identifying service when condition starts at " + conditionStartDate);
        logger.trace("Services: " + String.join(";", services.stream().map(s -> s.toString()).collect(Collectors.toList())));
        Optional<Service> serviceDuringWhichConditionStarted = services.stream()
                .filter(s -> s.getStartDate().isBefore(conditionStartDate))
                .filter(s -> !s.getEndDate().isPresent() || s.getEndDate().get().isAfter(conditionStartDate))
                .findFirst();

        if (serviceDuringWhichConditionStarted.isPresent()) {
            logger.trace("Service during which condition started: " + serviceDuringWhichConditionStarted);
            return serviceDuringWhichConditionStarted;
        } else {
            logger.trace("No services which started and either ended before or were ongoing at condition start date, therefore finding immediately preceeding service...");
            Optional<Service> lastService = services.stream()
                    .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
                    .findFirst();
            logger.trace("Returning service: " + lastService);
            return lastService;
        }

    }


    public static long getNumberOfDaysOfOperationalServiceInInterval(OffsetDateTime startDate, OffsetDateTime endDate, ImmutableList<Deployment> deployments, Predicate<Deployment> isOperational) {
        logger.trace("Getting number of days of operational service in interval starting " + startDate + ", ending on " + endDate + ".");
        long days = deployments.stream()
                .filter(d -> isOperational.test(d))
                .map(d -> getElapsedDaysOfDeploymentInInterval(startDate, endDate, d.getStartDate(), d.getEndDate()))
                .collect(Collectors.summingLong(value -> value));

        logger.trace("returning total number of days of operational service of: " + days);
        return days;
    }

    private static long getElapsedDaysOfDeploymentInInterval(OffsetDateTime intervalStartDate, OffsetDateTime intervalEndDate, OffsetDateTime deploymentStartDate, Optional<OffsetDateTime> deploymentEndDate) {
        logger.trace("Getting elapsed days in interval with a deployment...");
        logger.trace("Interval start date: " + intervalStartDate);
        logger.trace("Interval end date: " + intervalEndDate);
        logger.trace("Deployment start date: " + deploymentStartDate);
        logger.trace("Deployment end date: " + deploymentEndDate);
        if (deploymentEndDate.isPresent() && deploymentEndDate.get().isBefore(intervalStartDate)) {
            logger.trace("Deployment end date is before start date, therefore returning 0 days.");
            return 0;
        }

        if (deploymentStartDate.isAfter(intervalEndDate)) {
            logger.trace("Deployment start date is after the interval end date, therefore returning 0 days.");
            return 0;
        }

        OffsetDateTime deploymentOrIntervalEndDate = getEarlierOfDeploymentEndDateOrIntervalEnd(intervalEndDate, deploymentEndDate);
        logger.trace("The earlier of the interval or deployment end date is " + deploymentOrIntervalEndDate);

        long days = getInclusiveNumberOfDaysBetween(deploymentStartDate, deploymentOrIntervalEndDate);
        logger.trace("Number of days between the deployment start date and interval end date: " + days);
        return days;
    }

    private static long getInclusiveNumberOfDaysBetween(OffsetDateTime start, OffsetDateTime end) {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    private static OffsetDateTime getEarlierOfDeploymentEndDateOrIntervalEnd(OffsetDateTime intervalEndDate, Optional<OffsetDateTime> deploymentEndDate) {
        if (!deploymentEndDate.isPresent()) {
            logger.trace("No deployment end date, therefore using interval end date.");
            return intervalEndDate;
        }

        if (deploymentEndDate.get().isBefore(intervalEndDate)) {
            logger.trace("Deployment ends before interval, therefore using deployment end date.");
            return deploymentEndDate.get();
        }
        return intervalEndDate;
    }

    public static ImmutableList<Deployment> getDeployments(ServiceHistory history) {
        ImmutableList<Deployment> deployments = history.getServices().stream()
                .flatMap(s -> s.getDeployments().stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        logger.trace("Number of deployments in service history: " + deployments.size());

        return deployments;
    }

    public static Rank getRankProximateToDate(ImmutableSet<Service> services, OffsetDateTime testDate) {
        // todo: find most 'beneficial rank': army first, if there is at least 1 year of service, then if any service of navy,
        // use navy rank,
        logger.trace("Getting the rank on the last service before date " + testDate);
        Optional<Service> relevantService = services.stream()
                .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate())) // most recent first
                .filter(service -> service.getStartDate().isBefore(testDate))
                .findFirst();

        if (!relevantService.isPresent())
            throw new ProcessingRuleError(String.format("No service starting before date: %s.", testDate));

        return relevantService.get().getRank();
    }

    public static Long getDaysOfContinuousFullTimeServiceToDate(ServiceHistory serviceHistory, OffsetDateTime toDate) {

        Long days = serviceHistory.getServices().stream()
                .filter(service -> service.getStartDate().isBefore(toDate))
                .map(service -> getDurationOfServiceCFTSInDays(service, toDate))
                .collect(Collectors.summingLong(value -> value));

        return days;
    }

    private static Long getDurationOfServiceCFTSInDays(Service service, OffsetDateTime endDate) {
        logger.trace("Counting number of days CFTS to " + endDate + "...");
        if (service.getEmploymentType() != EmploymentType.CTFS) {
            logger.trace("Employment type is not CFTS, therefore returning 0 days.");
            return 0L;
        } else {
            OffsetDateTime startDate = service.getStartDate();
            if (service.getEndDate().isPresent() && service.getEndDate().get().isBefore(endDate)) {
                logger.trace("Service end date is present and before the test date, therefore counting days between start of service and test date...");
                OffsetDateTime serviceEndDate = DateTimeUtils.toMidnightAmNextDay(service.getEndDate().get());
                Duration duration = Duration.between(startDate, serviceEndDate);
                long days = duration.toDays();
                logger.trace("Returning days counted: " + days);
                return days;
            }

            logger.trace("Service onging at test date, therefore counting days between start of service and test date...");
            OffsetDateTime nextDayMidnightOfSpecifiedEndDate = DateTimeUtils.toMidnightAmNextDay(endDate);
            long days = Duration.between(startDate, nextDayMidnightOfSpecifiedEndDate).toDays();
            logger.trace("Returning days counted: " + days);
            return days;
        }
    }


    public static ImmutableList<FactorWithSatisfaction> withSatsifiedFactors(ImmutableList<Factor> factors, String... factorParagraphs) {
        ImmutableSet<String> specifiedFactors = ImmutableSet.copyOf(Arrays.stream(factorParagraphs).collect(Collectors.toList()));

        ImmutableList<FactorWithSatisfaction> factorsWithSatisfaction = factors.stream()
                .map(factor -> specifiedFactors.contains(factor.getParagraph()) ? new FactorWithSatisfactionImpl(factor, true) : new FactorWithSatisfactionImpl(factor, false))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        return factorsWithSatisfaction;
    }


    public static Predicate<Deployment> getIsOperationalPredicate(ServiceDeterminationPair serviceDeterminationPair) {
        ImmutableList<Operation> allOperations = ImmutableList.copyOf(Iterables.concat(
                serviceDeterminationPair.getWarlike().getOperations(),
                serviceDeterminationPair.getNonWarlike().getOperations()));

        List<String> opNames = allOperations.stream()
                .map(operation -> operation.getName())
                .map(name -> name.toLowerCase())
                .distinct()
                .collect(Collectors.toList());

        ImmutableSet<String> setOfLowerCaseOpNames = ImmutableSet.copyOf(opNames);

        return (deploymentName -> {
            String lowerCasedeploymentNameWithoutOperation = deploymentName.getOperationName().toLowerCase().replace("operation", "").trim();
            return setOfLowerCaseOpNames.contains(lowerCasedeploymentNameWithoutOperation);
        });
    }

    public static Boolean conditionIsBeforeService(Condition condition, ServiceHistory serviceHistory)
    {
        return condition.getStartDate().isBefore(serviceHistory.getHireDate());
    }

}


