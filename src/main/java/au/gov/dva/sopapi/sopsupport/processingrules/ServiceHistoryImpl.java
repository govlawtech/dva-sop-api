package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;


public class ServiceHistoryImpl implements ServiceHistory {

    private final OffsetDateTime hireDate;
    private final ImmutableSet<Service> services;

    public ServiceHistoryImpl(OffsetDateTime hireDate, ImmutableSet<Service> services) {

        this.hireDate = hireDate;
        this.services = services;
    }

    @Override
    public OffsetDateTime getHireDate() {
        return hireDate;
    }

    @Override
    public ImmutableSet<Service> getServices() {
        return services;
    }
}
