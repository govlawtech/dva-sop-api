package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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

    @Override
    public ServiceHistory filterServiceHistoryByEvents(List<String> eventList) {
        ArrayList newServices = new ArrayList();
        for (Service service : services) {
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
        return new ServiceHistoryImpl(hireDate, ImmutableSet.copyOf(newServices));
    }
}
