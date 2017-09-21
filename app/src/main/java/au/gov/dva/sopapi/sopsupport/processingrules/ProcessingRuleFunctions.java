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


    public static Optional<LocalDate> getStartofService(ServiceHistory serviceHistory) {
        Optional<Service> earliestService = serviceHistory.getServices().stream()
                .sorted(Comparator.comparing(Service::getStartDate))
                .findFirst();
        return earliestService.map(Service::getStartDate);
    }

    /**
     * Returns a date a certain period before the given onset date, depending on the period specifier
     *
     * @param periodSpecifier - indicate the period to back in time. e.g. 2d = 2 days, 3y = 3 years
     * @param onsetDate       - The date to start counting back from
     * @return The date which is the specified amount back in time e.g. ("7d", 2011-04-14) -> 2011-04-07
     */
    public static LocalDate getStartOfOnsetWindow(String periodSpecifier, LocalDate onsetDate) {
        if (periodSpecifier == null || !periodSpecifier.trim().matches("^\\d+(d|y)$"))
            throw new RuntimeException("period specifier is not valid, expecting 2d, 3y etc. : " + periodSpecifier);
        if (onsetDate == null)
            throw new RuntimeException("onsetDate cannot be null");

        String trimmed = periodSpecifier.trim();
        int index = trimmed.indexOf('d');
        if (index == -1) {
            index = trimmed.indexOf('y');
            int numYears = Integer.parseInt(trimmed.substring(0, index));
            return onsetDate.minusYears(numYears);
        } else {
            int numDays = Integer.parseInt(trimmed.substring(0, index));
            return onsetDate.minusDays(numDays);
        }
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

    public static long getNumberOfDaysOfServiceInInterval(LocalDate startDate, LocalDate endDate, ImmutableList<? extends HasDateRange> deploymentsOrService) {
        List<HasDateRange> flattened = DateTimeUtils.flattenDateRanges(new ArrayList<>(deploymentsOrService));
        long days = flattened.stream()
                .map(d -> getInclusiveDaysFromRangeInInterval(startDate, endDate, d))
                .collect(Collectors.summingLong(value -> value));

        return days;
    }

    // sorted latest first
    public static ImmutableList<Interval> getIntervalsWithMaximumService(int intervalDurationInCalendarYears, LocalDate lowerBoundary, LocalDate upperBoundaryInclusive, ImmutableList<? extends HasDateRange> deploymentsOrService) {
        List<Interval> testIntervals = Intervals.getSopFactorTestIntervalsJavaList(intervalDurationInCalendarYears, lowerBoundary, upperBoundaryInclusive);
        assert (testIntervals.size() > 0);
        if (testIntervals.size() > 1) {
            Comparator<Interval> longestFirst = (o1, o2) -> Long.compare(
                    getNumberOfDaysOfServiceInInterval(o2.getStart(), o2.getEnd(), deploymentsOrService),
                    getNumberOfDaysOfServiceInInterval(o1.getStart(), o1.getEnd(), deploymentsOrService));

            Comparator<Interval> latestFirst = (o1, o2) -> o2.getEnd().compareTo(o1.getEnd());

            List<Interval> intervalsSortedByOpServiceThenLatest = testIntervals.stream()
                    .sorted(longestFirst.thenComparing(latestFirst)).collect(Collectors.toList());

            Interval head = intervalsSortedByOpServiceThenLatest.get(0);
            long maxOpServiceDays = getNumberOfDaysOfServiceInInterval(head.getStart(), head.getEnd(), deploymentsOrService);


            List<Interval> withLesserDropped = intervalsSortedByOpServiceThenLatest.stream()
                    .filter(interval -> getNumberOfDaysOfServiceInInterval(interval.getStart(),
                            interval.getEnd(), deploymentsOrService) == maxOpServiceDays).collect(Collectors.toList());

            return ImmutableList.copyOf(withLesserDropped);
        } else {
            return ImmutableList.of(new Interval(lowerBoundary, upperBoundaryInclusive));
        }
    }

    private static long getInclusiveDaysFromRangeInInterval(LocalDate intervalStartDate, LocalDate intervalEndDate, HasDateRange dateRange) {

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

    public static ImmutableList<Service> getCFTSServices(ServiceHistory serviceHistory) {
        return serviceHistory.getServices()
                .stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }


    public static ImmutableList<Deployment> getCFTSDeployments(ServiceHistory history) {
        ImmutableList<Deployment> deployments = history.getServices().stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .flatMap(s -> s.getDeployments().stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        return deployments;
    }

    public static Optional<Rank> getCFTSRankProximateToDate(ImmutableSet<Service> services, LocalDate testDate, CaseTrace caseTrace) {

        // caseTrace.addLoggingTrace("Getting the rank on the last service before date " + testDate);
        Optional<Service> relevantService = services.stream()
                .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate())) // most recent first
                .filter(service -> !service.getStartDate().isAfter(testDate) && service.getEmploymentType() == EmploymentType.CFTS)
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

    public static Boolean conditionIsBeforeHireDate(Condition condition, ServiceHistory serviceHistory) {
        return condition.getStartDate().isBefore(serviceHistory.getHireDate());

    }

    public static boolean conditionIsBeforeFirstDateOfService(Condition condition, ServiceHistory serviceHistory) {
        if (!serviceHistory.getStartofService().isPresent()) return true;
        return condition.getStartDate().isBefore(serviceHistory.getStartofService().get());
    }
}







