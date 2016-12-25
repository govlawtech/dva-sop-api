package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.exceptions.ProcessingRuleError;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProcessingRuleFunctions {


    public static Optional<Service> identifyServiceDuringOrAfterWhichConditionOccurs(ImmutableSet<Service> services, OffsetDateTime conditionStartDate) {

        Optional<Service> serviceDuringWhichConditionStarted = services.stream()
                .filter(s -> s.getStartDate().isBefore(conditionStartDate))
                .filter(s -> !s.getEndDate().isPresent() || s.getEndDate().get().isAfter(conditionStartDate))
                .findFirst();

        if (serviceDuringWhichConditionStarted.isPresent())
            return serviceDuringWhichConditionStarted;

        else
        {
            Optional<Service> lastService = services.stream()
                    .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
                    .findFirst();

            return lastService;
        }

    }


    public static long getNumberOfDaysOfOperationalServiceInInterval(OffsetDateTime startDate, OffsetDateTime endDate, ImmutableList<Deployment> deployments, Predicate<Deployment> isOperational)
    {
        long days = deployments.stream()
                .filter(d -> isOperational.test(d))
                .map(d -> getElapsedDaysOfDeploymentInInterval(startDate,endDate,d.getStartDate(),d.getEndDate()))
                .collect(Collectors.summingLong(value -> value));

        return days;
    }

    private static long getElapsedDaysOfDeploymentInInterval(OffsetDateTime intervalStartDate, OffsetDateTime intervalEndDate, OffsetDateTime deploymentStartDate, Optional<OffsetDateTime> deploymentEndDate)
    {
         if (deploymentEndDate.isPresent() && deploymentEndDate.get().isBefore(intervalStartDate))
             return 0;

         if (deploymentStartDate.isAfter(intervalEndDate))
             return 0;

         OffsetDateTime deploymentOrIntervalEndDate = getEarlierOfDeploymentEndDateOrIntervalEnd(intervalEndDate,deploymentEndDate);

         return getInclusiveNumberOfDaysBetween(deploymentStartDate,deploymentOrIntervalEndDate);
    }

    private static long getInclusiveNumberOfDaysBetween(OffsetDateTime start, OffsetDateTime end)
    {
        return ChronoUnit.DAYS.between(start,end) + 1;
    }

    private static OffsetDateTime getEarlierOfDeploymentEndDateOrIntervalEnd(OffsetDateTime intervalEndDate, Optional<OffsetDateTime> deploymentEndDate)
    {
        if (!deploymentEndDate.isPresent())
            return intervalEndDate;

        if (deploymentEndDate.get().isBefore(intervalEndDate))
            return deploymentEndDate.get();

        return intervalEndDate;
    }

    public static ImmutableList<Deployment> getDeployments(ServiceHistory history)
    {
        return history.getServices().stream()
                .flatMap(s -> s.getDeployments().stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));
    }

    public static Rank getRankProximateToDate(ImmutableSet<Service> services, OffsetDateTime testDate)
    {
         Optional<Service> relevantService =  services.stream()
                 .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
                 .filter(service -> service.getStartDate().isBefore(testDate))
                 .findFirst();

         if (!relevantService.isPresent())
             throw new ProcessingRuleError(String.format("No service on date: %s", testDate));

         return relevantService.get().getRank();
    }

    public static Long getDaysOfContinuousFullTimeServiceToDate(ServiceHistory serviceHistory, OffsetDateTime toDate)
    {

        Long days = serviceHistory.getServices().stream()
                .filter(service -> service.getStartDate().isBefore(toDate))
                .filter(service -> !service.getEndDate().isPresent() || service.getEndDate().get().isAfter(toDate))
                .map(service -> getDurationOfServiceCFTSInDays(service,toDate))
                .collect(Collectors.summingLong(value -> value));

        return days;
    }

    private static Long getDurationOfServiceCFTSInDays(Service service, OffsetDateTime endDate)
    {
        if (service.getEmploymentType() != EmploymentType.CTFS)
            return 0L;

        else {
            OffsetDateTime startDate = service.getStartDate();
            if (service.getEndDate().isPresent())
            {
                OffsetDateTime serviceEndDate = DateTimeUtils.toMidnightAmNextDay(service.getEndDate().get());
                Duration duration = Duration.between(startDate,serviceEndDate);
                return duration.toDays();
            }
            OffsetDateTime nextDayMidnightOfSpecifiedEndDate = DateTimeUtils.toMidnightAmNextDay(endDate);
            return Duration.between(startDate,nextDayMidnightOfSpecifiedEndDate).toDays();
        }
    }

    public static ImmutableList<Factor> getApplicableFactors(SoP sop, IncidentType incidentType)
    {
        ImmutableList<Factor> factors = (incidentType == IncidentType.Aggravation) ?
                sop.getAggravationFactors() : ((incidentType == IncidentType.Onset) ?
                sop.getOnsetFactors() : ImmutableList.of());

        return factors;
    }

    public static ImmutableList<FactorWithSatisfaction> withSatsifiedFactors(ImmutableList<Factor> factors, String... factorParagraphs)
    {
        ImmutableSet<String> specifiedFactors = ImmutableSet.copyOf(Arrays.stream(factorParagraphs).collect(Collectors.toList()));

        ImmutableList<FactorWithSatisfaction> factorsWithSatisfaction = factors.stream()
                .map(factor -> specifiedFactors.contains(factor.getParagraph()) ? new FactorWithSatisfactionImpl(factor,true) : new FactorWithSatisfactionImpl(factor,false))
                .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));

        return factorsWithSatisfaction;
    }


    public static Predicate<Deployment> getIsOperationalPredicate(ServiceDeterminationPair serviceDeterminationPair)
    {
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
            String lowerCasedeploymentNameWithoutOperation = deploymentName.getOperationName().toLowerCase().replace("operation","").trim();
            return setOfLowerCaseOpNames.contains(lowerCasedeploymentNameWithoutOperation);
        });
    }

}


