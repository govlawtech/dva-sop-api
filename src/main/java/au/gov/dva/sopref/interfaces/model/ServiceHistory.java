package au.gov.dva.sopref.interfaces.model;

import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;

public interface ServiceHistory {
    LocalDate getEnlistmentDate();
    LocalDate getSeparationDate();
    LocalDate getHireDate();
    ImmutableSet<Service> getServices();
    ImmutableSet<Operation> getOperations();
}
