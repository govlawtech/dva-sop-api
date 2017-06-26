package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.List;

public interface ServiceHistory {
    OffsetDateTime getHireDate();
    ImmutableSet<Service> getServices();
    ServiceHistory filterServiceHistoryByEvents(List<String> eventList);
}
