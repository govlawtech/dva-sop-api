package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

}
