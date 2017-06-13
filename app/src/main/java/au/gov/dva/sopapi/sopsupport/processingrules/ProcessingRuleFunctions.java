package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.exceptions.ProcessingRuleRuntimeException;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopref.datecalcs.Intervals;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessingRuleFunctions {

    private static Logger logger = LoggerFactory.getLogger(ProcessingRuleFunctions.class.getSimpleName());

    public static Optional<OffsetDateTime> getFirstOperationalServiceStartDate(ServiceHistory serviceHistory, Predicate<Deployment> isOperational) {
        Optional<OffsetDateTime> start = ProcessingRuleFunctions.getDeployments(serviceHistory)
                .stream()
                .filter(isOperational::test)
                .sorted(Comparator.comparing(Deployment::getStartDate))
                .map(Deployment::getStartDate)
                .findFirst();

        return start;
    }

    public static Optional<OffsetDateTime> getStartofService(ServiceHistory serviceHistory) {
        Optional<Service> earliestService = serviceHistory.getServices().stream()
                .sorted(Comparator.comparing(Service::getStartDate))
                .findFirst();
        return earliestService.map(Service::getStartDate);
    }

    public static Optional<Service> identifyServiceDuringOrAfterWhichConditionOccurs(ImmutableSet<Service> services, OffsetDateTime conditionStartDate, CaseTrace caseTrace) {

        Optional<Service> serviceDuringWhichConditionStarted = services.stream()
                .filter(s -> s.getStartDate().isBefore(conditionStartDate))
                .filter(s -> !s.getEndDate().isPresent() || s.getEndDate().get().isAfter(conditionStartDate))
                .findFirst();

        if (serviceDuringWhichConditionStarted.isPresent()) {
        //    caseTrace.addLoggingTrace("Service during which condition started: " + serviceDuringWhichConditionStarted.get());
            return serviceDuringWhichConditionStarted;
        } else {
//            caseTrace.addLoggingTrace("No services which started before and were ongoing at the condition start date, therefore finding immediately preceding service, if any.");
            Optional<Service> lastService = services.stream()
                    .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
                    .findFirst();
            return lastService;
        }
    }

    public static long getNumberOfDaysOfOperationalServiceInInterval(OffsetDateTime startDate, OffsetDateTime endDate, ImmutableList<Deployment> deployments, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        caseTrace.addLoggingTrace("Getting number of days of operational service in interval starting " + startDate + ", ending on " + endDate + ".");
        long days = deployments.stream()
                .filter(d -> isOperational.test(d))
                .map(d -> getElapsedDaysOfDeploymentInInterval(startDate, endDate, d.getStartDate(), d.getEndDate()))
                .collect(Collectors.summingLong(value -> value));

        //caseTrace.addLoggingTrace("Total number of days of operational service: " + days);
        return days;
    }


    public static long getMaximumDaysOfOpServiceInAnyInterval(int intervalDurationInYears, OffsetDateTime startDate, OffsetDateTime endDate, ImmutableList<Deployment> deployments, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        List<Interval> testIntervals = Intervals.getSopFactorTestIntervalsJavaList(intervalDurationInYears, startDate, endDate);
        assert (testIntervals.size() > 0);
        if (testIntervals.size() > 1) {
            caseTrace.addLoggingTrace(String.format("Number of intervals of %s years between %s and %s: %s", intervalDurationInYears, startDate, endDate, testIntervals.size()));
            Comparator<Interval> longestFirst = (o1, o2) -> Long.compare(
                    getNumberOfDaysOfOperationalServiceInInterval(o2.getStart(), o2.getEnd(), deployments, isOperational, caseTrace),
                    getNumberOfDaysOfOperationalServiceInInterval(o1.getStart(), o1.getEnd(), deployments, isOperational, caseTrace));

            Comparator<Interval> latestFirst = (o1, o2) -> o2.getEnd().compareTo(o1.getEnd());

            List<Interval> intervalsSortedByOpServiceThenLatest = testIntervals.stream()
                    .sorted(longestFirst.thenComparing(latestFirst))
                    .collect(Collectors.toList());

            Interval chosenInterval = intervalsSortedByOpServiceThenLatest.get(0);
            caseTrace.addLoggingTrace(String.format("Interval with the most operational service starts on %s and ends on %s", chosenInterval.getStart(), chosenInterval.getEnd()));
            return getNumberOfDaysOfOperationalServiceInInterval(chosenInterval.getStart(), chosenInterval.getEnd(), deployments, isOperational, caseTrace);
        }

        return getNumberOfDaysOfOperationalServiceInInterval(startDate, endDate, deployments, isOperational, caseTrace);

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

        long days = getExclusiveNumberOfDaysBetween(deploymentStartDate, deploymentOrIntervalEndDate);
        logger.trace("Number of days between the deployment start date and interval end date: " + days);
        return days;
    }

    private static long getExclusiveNumberOfDaysBetween(OffsetDateTime start, OffsetDateTime end) {
        return ChronoUnit.DAYS.between(start, end);
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

    public static Optional<Rank> getRankProximateToDate(ImmutableSet<Service> services, OffsetDateTime testDate, CaseTrace caseTrace) {

       // caseTrace.addLoggingTrace("Getting the rank on the last service before date " + testDate);
        Optional<Service> relevantService = services.stream()
                .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate())) // most recent first
                .filter(service -> service.getStartDate().isBefore(testDate))
                .findFirst();


        if (!relevantService.isPresent()) {
            caseTrace.addLoggingTrace(String.format("No service starting before date: %s.", testDate));
            return Optional.empty();
        } else {
//            caseTrace.addLoggingTrace("Relevant rank: " + relevantService.get().getRank());
            Rank rank = relevantService.get().getRank();
            return Optional.ofNullable(rank);
        }

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

            logger.trace("Service ongoing at test date, therefore counting days between start of service and test date...");
            OffsetDateTime nextDayMidnightOfSpecifiedEndDate = DateTimeUtils.toMidnightAmNextDay(endDate);
            long days = Duration.between(startDate, nextDayMidnightOfSpecifiedEndDate).toDays();
            logger.trace("Returning days counted: " + days);
            return days;
        }
    }


    public static ImmutableList<FactorWithSatisfaction> withSatisfiedFactors(ImmutableList<Factor> factors, ImmutableSet<String> factorParagraphs) {

        ImmutableList<FactorWithSatisfaction> factorsWithSatisfaction = factors.stream()
                .map(factor -> factorParagraphs.contains(factor.getParagraph()) ? new FactorWithSatisfactionImpl(factor, true) : new FactorWithSatisfactionImpl(factor, false))
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


    public static Boolean conditionIsBeforeService(Condition condition, ServiceHistory serviceHistory) {
        return condition.getStartDate().isBefore(serviceHistory.getHireDate());

    }

    public static boolean conditionStartedWithinXYearsOfLastDayOfMRCAService(Condition condition, ServiceHistory serviceHistory, int numberOfYears, CaseTrace caseTrace) {

        OffsetDateTime lastTimeOfMRCAService = getLastTimeOfMRCAServiceOrDefault(serviceHistory, OffsetDateTime.now());
        caseTrace.addLoggingTrace("The last time of MRCA service or now (if service is ongoing): " + lastTimeOfMRCAService);

        OffsetDateTime midnightNextDayAfterLastTimeOfMRCAService = DateTimeUtils.toMidnightAmNextDay(lastTimeOfMRCAService);

        OffsetDateTime xYearsFromLastDayOfMrcaService = midnightNextDayAfterLastTimeOfMRCAService.plusYears(numberOfYears);
        caseTrace.addLoggingTrace(String.format("The time %s years from the last day of MRCA service: %s", numberOfYears, xYearsFromLastDayOfMrcaService));

        OffsetDateTime conditionStartDate = condition.getStartDate();
        OffsetDateTime midnightAmOnDayOfConditionStart = DateTimeUtils.toMightnightAmThisDay(conditionStartDate);
        caseTrace.addLoggingTrace("Midnight AM on the day the condition started: " + midnightAmOnDayOfConditionStart);

        return midnightAmOnDayOfConditionStart.isBefore(xYearsFromLastDayOfMrcaService);
    }

    private static OffsetDateTime getLastTimeOfMRCAServiceOrDefault(ServiceHistory serviceHistory, OffsetDateTime defaultDate) {
        Stream<Service> servicesOrderedMostRecentFirst = serviceHistory.getServices().asList()
                .stream()
                .sorted((o1, o2) -> o2.getStartDate().compareTo(o2.getStartDate()));

        Optional<Service> mostRecentService = servicesOrderedMostRecentFirst.findFirst();
        // no service
        if (!mostRecentService.isPresent())
            throw new ProcessingRuleRuntimeException("No services in service history.");

            // ongoing
        else if (!mostRecentService.get().getEndDate().isPresent()) {
            return defaultDate;
        } else {
            return mostRecentService.get().getEndDate().get();
        }
    }


}




