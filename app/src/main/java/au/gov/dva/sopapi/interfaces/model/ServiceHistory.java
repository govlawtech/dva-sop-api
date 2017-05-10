package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;

public interface ServiceHistory {
    OffsetDateTime getHireDate();
    ImmutableSet<Service> getServices();
}
