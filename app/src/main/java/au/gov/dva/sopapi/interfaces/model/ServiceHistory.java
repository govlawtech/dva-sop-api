package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.exceptions.ProcessingRuleRuntimeException;
import au.gov.dva.sopapi.exceptions.ServiceHistoryCorruptException;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import au.gov.dva.sopapi.sopsupport.processingrules.ServiceHistoryImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.ServiceImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;


import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ServiceHistory {
    LocalDate getHireDate();
    ImmutableSet<Service> getServices();
    default Optional<LocalDate> getStartofService() {
        Optional<Service> earliestService = this.getServices().stream()
                .sorted(Comparator.comparing(Service::getStartDate))
                .findFirst();
        return earliestService.map(Service::getStartDate);
    }

    default ServiceHistory filterServiceHistoryByEvents(List<String> eventList) {


        ArrayList<Service> newServices = new ArrayList<>();
        for (Service service : getServices()) {
            ImmutableSet<Deployment> deployments = ImmutableSet.copyOf(
                    service.getDeployments().stream()
                            .filter(d -> d.getEvent() != null && eventList.contains(d.getEvent().trim().toLowerCase()))
                            .collect(Collectors.toList())
            );
            newServices.add(new ServiceImpl(
                    service.getBranch()
                    , service.getEmploymentType()
                    , service.getRank()
                    , service.getStartDate()
                    , service.getEndDate()
                    , deployments
            ));
        }
        return new ServiceHistoryImpl(getHireDate(), ImmutableSet.copyOf(newServices));
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


        if (deployments.size() > 1) {
            String deploymentStarts = String.join(",",deployments.stream().map(d ->  d.getStartDate().toString()).collect(Collectors.toList()));

                throw new ServiceHistoryCorruptException(String.format("Service history data appears corrupt: shows two or more concurrent deployments: the deployments starting %s",deploymentStarts));
        }

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
