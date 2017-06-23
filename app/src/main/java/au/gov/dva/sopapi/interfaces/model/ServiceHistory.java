package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface ServiceHistory {
    LocalDate getHireDate();
    ImmutableSet<Service> getServices();
}
