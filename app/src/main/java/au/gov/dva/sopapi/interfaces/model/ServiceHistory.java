package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.exceptions.ProcessingRuleRuntimeException;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;


import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ServiceHistory {
    LocalDate getHireDate();
    ImmutableSet<Service> getServices();
    ServiceHistory filterServiceHistoryByEvents(List<String> eventList);
    default Optional<LocalDate> getStartofService() {
        Optional<Service> earliestService = this.getServices().stream()
                .sorted(Comparator.comparing(Service::getStartDate))
                .findFirst();
        return earliestService.map(Service::getStartDate);
    }

    default long getNumberOfDaysOfFullTimeOperationalService(LocalDate startDate, LocalDate endDateInclusive, Predicate<Deployment> isOperational)
    {
        ImmutableList<Deployment> operationalDeployments =
                this.getCftsDeployments().stream()
                        .filter(d -> isOperational.test(d))
                        .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        Long daysOfOperationalService = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(startDate, endDateInclusive, operationalDeployments);
        return daysOfOperationalService;
    }

    default ImmutableList<Deployment> getCftsDeployments()
    {
        return ProcessingRuleFunctions.getCFTSDeployments(this);
    }

    default long getNumberOfDaysCftsInIntervalInclusive(LocalDate startDateInclusive, LocalDate endDateInclusive) {

        if (!this.getStartofService().isPresent()) return 0;

        ImmutableList<Service> cftsServices = this.getServices().stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        Long actualDaysOfCfts = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(startDateInclusive, endDateInclusive, cftsServices);

        return actualDaysOfCfts;
    }

    // todo: interval tree
    default Optional<Deployment> findDeploymentOnDate(LocalDate testDate) {
        List<Deployment> deployments = this.getServices().stream()
                .flatMap(s -> s.getDeployments().stream())
                .filter(d -> d.getStartDate().isBefore(testDate) || d.getStartDate().isEqual(testDate))
                .filter(d -> !d.getEndDate().isPresent() || d.getEndDate().get().isEqual(testDate) || d.getEndDate().get().isAfter(testDate))
                .collect(Collectors.toList());


        if (deployments.size() > 1) throw new ProcessingRuleRuntimeException("Service history data appears corrupt: shows two or more concurrent deployments");

        if (deployments.size() == 1)
        {
            return Optional.of(deployments.get(0));
        }
        else return Optional.empty();

    }


    default Optional<Service> findCftsOnDate(LocalDate localDate)
    {
        Optional<Service> serviceDuringWhichConditionStarted = this.getServices().stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .filter(s -> s.getStartDate().isBefore(localDate) || s.getStartDate().isEqual(localDate))
                .filter(s -> !s.getEndDate().isPresent() || s.getEndDate().get().isAfter(localDate) || s.getEndDate().get().isEqual(localDate))
                .findFirst();

        return serviceDuringWhichConditionStarted;
    }

}
