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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessingRuleFunctions {

    private static Logger logger = LoggerFactory.getLogger(ProcessingRuleFunctions.class.getSimpleName());

    public static Optional<LocalDate> getFirstOperationalServiceStartDate(ServiceHistory serviceHistory, Predicate<Deployment> isOperational) {
        Optional<LocalDate> start = ProcessingRuleFunctions.getCFTSDeployments(serviceHistory)
                .stream()
                .filter(isOperational::test)
                .sorted(Comparator.comparing(Deployment::getStartDate))
                .map(Deployment::getStartDate)
                .findFirst();

        return start;
    }

    public static Optional<LocalDate> getStartofService(ServiceHistory serviceHistory) {
        Optional<Service> earliestService = serviceHistory.getServices().stream()
                .sorted(Comparator.comparing(Service::getStartDate))
                .findFirst();
        return earliestService.map(Service::getStartDate);
    }

    public static Optional<Service> identifyCFTSServiceDuringOrAfterWhichConditionOccurs(ImmutableSet<Service> services, LocalDate conditionStartDate, CaseTrace caseTrace) {

        Optional<Service> serviceDuringWhichConditionStarted = services.stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .filter(s -> s.getStartDate().isBefore(conditionStartDate))
                .filter(s -> !s.getEndDate().isPresent() || s.getEndDate().get().isAfter(conditionStartDate))
                .findFirst();

        if (serviceDuringWhichConditionStarted.isPresent()) {
        //    caseTrace.addLoggingTrace("Service during which condition started: " + serviceDuringWhichConditionStarted.get());
            return serviceDuringWhichConditionStarted;
        } else {
//            caseTrace.addLoggingTrace("No services which started before and were ongoing at the condition start date, therefore finding immediately preceding service, if any.");
            Optional<Service> lastService = services.stream()
                    .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                    .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
                    .findFirst();
            return lastService;
        }
    }

    public static long getNumberOfDaysOfOperationalServiceInInterval(LocalDate startDate, LocalDate endDate, ImmutableList<Deployment> deployments, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        List<HasDateRange> toFlatten = deployments.stream()
                .filter(d -> isOperational.test(d))
                .collect(Collectors.toList());
        List<HasDateRange> flattened = DateTimeUtils.flattenDateRanges(toFlatten);
        long days = flattened.stream()
                .map(d -> getElapsedDaysOfDateRangeInInterval(startDate, endDate, d))
                .collect(Collectors.summingLong(value -> value));

        return days;
    }

    public static long getMaximumDaysOfOpServiceInAnyInterval(int intervalDurationInYears, LocalDate startDate, LocalDate endDate, ImmutableList<Deployment> deployments, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
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

    private static long getElapsedDaysOfDateRangeInInterval(LocalDate intervalStartDate, LocalDate intervalEndDate, HasDateRange dateRange) {
        logger.trace("Getting elapsed days in interval with a deployment...");
        logger.trace("Interval start date: " + intervalStartDate);
        logger.trace("Interval end date: " + intervalEndDate);
        logger.trace("Date range start date: " + dateRange.getStartDate());
        logger.trace("Date range end date: " + dateRange.getEndDate());
        if (dateRange.getEndDate().isPresent() && dateRange.getEndDate().get().isBefore(intervalStartDate)) {
            logger.trace("date range end date is before start date, therefore returning 0 days.");
            return 0;
        }

        if (dateRange.getStartDate().isAfter(intervalEndDate)) {
            logger.trace("Date range start date is after the interval end date, therefore returning 0 days.");
            return 0;
        }

        LocalDate dateRangeOrIntervalEndDate = getEarlierOfDateRangeEndDateOrIntervalEnd(intervalEndDate, dateRange.getEndDate());
        logger.trace("The earlier of the interval or date range end date is " + dateRangeOrIntervalEndDate);

        LocalDate dateRangeOrIntervalStartDate = intervalStartDate.isAfter(dateRange.getStartDate()) ?
                intervalStartDate : dateRange.getStartDate();
        logger.trace("The later of the interval or date range start date is " + dateRangeOrIntervalStartDate);

        long days = ChronoUnit.DAYS.between(dateRangeOrIntervalStartDate, dateRangeOrIntervalEndDate) + 1;  // Plus one for inclusive dates
        logger.trace("Number of days between the date range start date and interval end date: " + days);
        return days;
    }

    private static LocalDate getEarlierOfDateRangeEndDateOrIntervalEnd(LocalDate intervalEndDate, Optional<LocalDate> dateRangeEndDate) {
        if (!dateRangeEndDate.isPresent()) {
            logger.trace("No date range end date, therefore using interval end date.");
            return intervalEndDate;
        }

        if (dateRangeEndDate.get().isBefore(intervalEndDate)) {
            logger.trace("Date range ends before interval, therefore using deployment end date.");
            return dateRangeEndDate.get();
        }
        return intervalEndDate;
    }


    public static ImmutableList<Deployment> getCFTSDeployments(ServiceHistory history) {
        ImmutableList<Deployment> deployments = history.getServices().stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .flatMap(s -> s.getDeployments().stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        logger.trace("Number of deployments in service history: " + deployments.size());

        return deployments;
    }

    public static Optional<Rank> getCFTSRankProximateToDate(ImmutableSet<Service> services, LocalDate testDate, CaseTrace caseTrace) {

       // caseTrace.addLoggingTrace("Getting the rank on the last service before date " + testDate);
        Optional<Service> relevantService = services.stream()
                .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate())) // most recent first
                .filter(service -> service.getStartDate().isBefore(testDate) && service.getEmploymentType() == EmploymentType.CFTS)
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

    public static Long getDaysOfContinuousFullTimeServiceToDate(ServiceHistory serviceHistory, LocalDate toDate) {

        List<HasDateRange> toFlatten = serviceHistory.getServices().stream()
                .filter(service -> !service.getStartDate().isAfter(toDate) && service.getEmploymentType() == EmploymentType.CFTS)
                .collect(Collectors.toList());
        List<HasDateRange> flattened = DateTimeUtils.flattenDateRanges(toFlatten);

        Long days = flattened.stream()
                .map(dateRange -> getDaysInDateRangeUpTo(dateRange, toDate))
                .collect(Collectors.summingLong(value -> value));

        return days;
    }

    private static Long getDaysInDateRangeUpTo(HasDateRange dateRange, LocalDate endDate) {
        logger.trace("Counting number of days in date range to " + endDate + "...");
        LocalDate startDate = dateRange.getStartDate();
        long days;
        if (dateRange.getEndDate().isPresent() && dateRange.getEndDate().get().isBefore(endDate)) {
            logger.trace("Date range end date is present and before the test date, therefore counting days between start of date range and test date...");
            days = ChronoUnit.DAYS.between(startDate, dateRange.getEndDate().get()) + 1; // +1 for inclusive days
        }
        else {
            logger.trace("Date range ongoing at test date, therefore counting days between start of date range and test date...");
            days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // +1 for inclusive days
        }

        logger.trace("Returning days counted: " + days);
        return days;
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
        LocalDate lastDateOfMRCAService = getLastDateOfMRCAServiceOrDefault(serviceHistory, LocalDate.now());
        caseTrace.addLoggingTrace("The last date of MRCA service or now (if service is ongoing): " + lastDateOfMRCAService);

        LocalDate xYearsFromLastDayOfMrcaService = lastDateOfMRCAService.plusYears(numberOfYears);
        caseTrace.addLoggingTrace(String.format("The date %s years from the last day of MRCA service: %s", numberOfYears, xYearsFromLastDayOfMrcaService));

        LocalDate conditionStartDate = condition.getStartDate();
        caseTrace.addLoggingTrace("The day the condition started: " + conditionStartDate);

        return !conditionStartDate.isAfter(xYearsFromLastDayOfMrcaService);
    }

    private static LocalDate getLastDateOfMRCAServiceOrDefault(ServiceHistory serviceHistory, LocalDate defaultDate) {
        Stream<Service> servicesOrderedMostRecentFirst = serviceHistory.getServices().asList()
                .stream()
                .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()));

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




