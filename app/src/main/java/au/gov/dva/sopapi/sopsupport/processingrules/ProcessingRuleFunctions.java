package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.sopsupport.Act;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopref.datecalcs.Intervals;
import au.gov.dva.sopapi.sopsupport.processingrules.rules.SatisfiedFactorWithApplicablePart;
import au.gov.dva.sopapi.veaops.Facade;
import au.gov.dva.sopapi.veaops.interfaces.VeaOperationalServiceRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProcessingRuleFunctions {

    private static Logger logger = LoggerFactory.getLogger(ProcessingRuleFunctions.class.getSimpleName());


    // Note: this assumes that conditions onset after the MRCA commencement date relate to MRCA service -- not strictly correct
    // Also assumes no DRCA cases sent
    public static Act InferApplicableAct(ServiceHistory serviceHistory, Condition condition) {
        LocalDate onsetDate = condition.getStartDate();
        LocalDate mrcaStartDate = LocalDate.of(2004,6,30);
        if (onsetDate.isAfter(mrcaStartDate))
        {
            return Act.Mrca;
        }
        else return Act.Vea;
    }



    public static void inferRelevantOperations(ServiceHistory serviceHistory, Condition condition, VeaOperationalServiceRepository veaOperationalServiceRepository, ImmutableSet<ServiceDetermination> serviceDeterminations, Predicate<Deployment> isOperational, CaseTrace caseTrace)
    {
        Interval relevantInterval = caseTrace.getTestInterval();
        if (relevantInterval == null)
        {
            throw new DvaSopApiRuntimeException("Relevant interval not set in case trace.");
        }

        Act applicableAct = ProcessingRuleFunctions.InferApplicableAct(serviceHistory,condition);

        List<Deployment> operationDeployments =
                ProcessingRuleFunctions.getCFTSDeployments(serviceHistory)
                        .stream().filter(isOperational)
                        .filter(d -> DateTimeUtils.OpenEndedTestIntervalOverlapsWithInterval(relevantInterval.getStart(),relevantInterval.getEnd(),d.getStartDate(),d.getEndDate()))
                        .collect(Collectors.toList());

        if (applicableAct == Act.Vea)
        {
                Iterable<JustifiedMilitaryActivity> matchingActivities = Facade.getMatchingActivities(true,operationDeployments, veaOperationalServiceRepository);
                caseTrace.SetRelevantOperations(ImmutableList.copyOf(matchingActivities));
        }

        else if (applicableAct == Act.Mrca)
        {
            List<JustifiedMilitaryActivity> matchingOps = Operations.getMatchingOperationsForDeployments(serviceDeterminations,operationDeployments);
            caseTrace.SetRelevantOperations(ImmutableList.copyOf(matchingOps));
        }
        else {
            throw new DvaSopApiRuntimeException("Unrecognised Act: " + applicableAct);
        }
    }

    public static Optional<LocalDate> getStartofService(ServiceHistory serviceHistory) {
        Optional<Service> earliestService = serviceHistory.getServices().stream()
                .sorted(Comparator.comparing(Service::getStartDate))
                .findFirst();
        return earliestService.map(Service::getStartDate);
    }

    public static ImmutableSet<Service> identifyAllServicesStartingBeforeConditionOnset(ImmutableSet<Service> services, LocalDate conditionStartDate, CaseTrace caseTrace)
    {
        List<Service> beforeOrAtSameTimeAsOnset = services.stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .filter(s -> s.getStartDate().isBefore(conditionStartDate))
                .collect(Collectors.toList());

        return ImmutableSet.copyOf(beforeOrAtSameTimeAsOnset);
    }

    public static Optional<Service> identifyCFTSServiceDuringOrAfterWhichConditionOccurs(ImmutableSet<Service> services, LocalDate conditionStartDate, CaseTrace caseTrace) {

        Optional<Service> serviceDuringWhichConditionStarted = services.stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .filter(s -> s.getStartDate().isBefore(conditionStartDate))
                .filter(s -> !s.getEndDate().isPresent() || s.getEndDate().get().isAfter(conditionStartDate))
                .findFirst();

        if (serviceDuringWhichConditionStarted.isPresent()) {
            //    caseTrace.addLoggingTrace("Service during which condition started: " + serviceDuringWhichConditionStarted.get());
//            caseTrace.addLoggingTrace("No services which started before and were ongoing at the condition start date, therefore finding immediately preceding service, if any.");
            return serviceDuringWhichConditionStarted;
        } else {
            Optional<Service> lastService = services.stream()
                    .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                    .filter(s -> !s.getStartDate().isAfter(conditionStartDate))
                    .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
                    .findFirst();
            return lastService;
        }
    }


    public static long getNumberOfDaysOfServiceInInterval(LocalDate startDate, LocalDate endDate, ImmutableList<? extends MaybeOpenEndedInterval> deploymentsOrService) {
        List<MaybeOpenEndedInterval> flattened = DateTimeUtils.flattenDateRanges(new ArrayList<>(deploymentsOrService));
        long days = flattened.stream()
                .map(d -> getInclusiveDaysFromRangeInInterval(startDate, endDate, d))
                .collect(Collectors.summingLong(value -> value));

        return days;
    }

    // sorted latest first
    public static ImmutableList<Interval> getIntervalsWithMaximumService(int intervalDurationInCalendarYears, LocalDate lowerBoundary, LocalDate upperBoundaryInclusive, ImmutableList<? extends MaybeOpenEndedInterval> deploymentsOrService) {
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

    private static long getInclusiveDaysFromRangeInInterval(LocalDate intervalStartDate, LocalDate intervalEndDate, MaybeOpenEndedInterval dateRange) {

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
            caseTrace.addLoggingTrace(String.format("No continuous full-time service starting before date: %s.", testDate));
            return Optional.empty();
        } else {
//            caseTrace.addLoggingTrace("Relevant rank: " + relevantService.get().getRank());
            Rank rank = relevantService.get().getRank();
            return Optional.ofNullable(rank);
        }
    }

    public static ImmutableList<FactorWithSatisfaction> withSatisfiedFactors(ImmutableList<Factor> factors, ImmutableSet<String> factorParagraphs, Function<String, Tuple2<String,String>> splitFactorReferenceToMainAndSubPart, BiFunction<String,Factor,Optional<String>> tryExtractSubPartText)
    {
        ImmutableSet<String> mainFactorReferences = factorParagraphs.stream().map(s -> splitFactorReferenceToMainAndSubPart.apply(s)._1())
                .collect(Collectors.collectingAndThen(Collectors.toSet(),ImmutableSet::copyOf));

        ImmutableList<FactorWithSatisfaction> factorsWithSatisfaction = withSatisfiedFactors(factors,mainFactorReferences);

        ImmutableList<FactorWithSatisfaction> withSubParts = factorsWithSatisfaction.stream()
                .map(factorWithSatisfaction -> {
                    if (!mainFactorReferences.contains(factorWithSatisfaction.getFactor().getParagraph())) // key search
                    {
                        return factorWithSatisfaction;
                    }
                    else {
                        Tuple2<String,String> factorReferenceParts = splitFactorReferenceToMainAndSubPart.apply(factorWithSatisfaction.getFactor().getParagraph());
                        String subPartRef = factorReferenceParts._2();
                        Optional<String> applicablePart = tryExtractSubPartText.apply(subPartRef,factorWithSatisfaction.getFactor());
                        if (applicablePart.isPresent())
                        {
                            return new SatisfiedFactorWithApplicablePart(factorWithSatisfaction,applicablePart.get());
                        }
                        return factorWithSatisfaction;
                    }
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));

        return withSubParts;
    }


    public static ImmutableList<FactorWithSatisfaction> withSatisfiedFactors(ImmutableList<Factor> factors, ImmutableSet<String> factorParagraphs)
    {

        // factor is satsified if there is a match in the start of any of the factor paragraphs
        ImmutableList<FactorWithSatisfaction> factorsWithSatisfaction = factors.stream()
                .map(factor -> factorParagraphs.contains(factor.getParagraph()) ? new FactorWithSatisfactionImpl(factor, true) : new FactorWithSatisfactionImpl(factor, false))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        return factorsWithSatisfaction;
    }



    public static Boolean conditionIsBeforeHireDate(SopSupportRequestDto sopSupportRequestDto, ServiceHistory serviceHistory) {
        return sopSupportRequestDto.get_conditionDto().get_incidentDateRangeDto().get_startDate().isBefore(serviceHistory.getHireDate());
    }

    public static boolean conditionIsBeforeFirstDateOfService(SopSupportRequestDto sopSupportRequestDto, ServiceHistory serviceHistory) {
        if (!serviceHistory.getStartofService().isPresent()) return true;
        return sopSupportRequestDto.get_conditionDto().get_incidentDateRangeDto().get_startDate().isBefore(serviceHistory.getStartofService().get());
    }
}

