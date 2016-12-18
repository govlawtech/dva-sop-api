package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.exceptions.ProcessingRuleError;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProcessingRuleFunctions {


    public static Optional<Service> identifyServiceDuringWhichConditionOccurs(ImmutableSet<Service> services, OffsetDateTime conditionStartDate) {

        Optional<Service> relevantService = services.stream()
                .filter(s -> s.getStartDate().isBefore(conditionStartDate))
                .filter(s -> !s.getEndDate().isPresent() || s.getEndDate().get().isAfter(conditionStartDate))
                .findFirst();

        return relevantService;
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
}


