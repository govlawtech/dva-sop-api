package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;

public interface Service {
    String getName();
    String getType();

    LocalDate getStartDate();
    LocalDate getEndDate();
    ImmutableSet<Deployment> getDeployments();
}
