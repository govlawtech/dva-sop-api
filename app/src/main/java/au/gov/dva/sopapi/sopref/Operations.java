package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.ServiceType;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Operations {

    public static Optional<ServiceDetermination> getLatestServiceDetermination(ImmutableSet<ServiceDetermination> allServiceDeterminations, ServiceType serviceType)
    {
        List<OffsetDateTime> commencementDates =  allServiceDeterminations.stream().map(sd -> sd.getCommencementDate()).collect(Collectors.toList());

        assert(commencementDates.size() == commencementDates.stream().distinct().count());

        return allServiceDeterminations.stream()
                .filter(sd -> sd.getServiceType().equals(serviceType))
                .sorted((o1, o2) -> o2.getCommencementDate().compareTo(o1.getCommencementDate()))
                .findFirst();
    }


    public static ServiceDeterminationPair getLatestDeterminationPair(ImmutableSet<ServiceDetermination> allServiceDeterminations)
    {
        Optional<ServiceDetermination> relevantWarlikeDetermination = Operations.getLatestServiceDetermination(allServiceDeterminations,ServiceType.WARLIKE);

        Optional<ServiceDetermination> relevantNonWarlikeDetermination = Operations.getLatestServiceDetermination(allServiceDeterminations,ServiceType.NON_WARLIKE);

        if (!relevantNonWarlikeDetermination.isPresent() || !relevantWarlikeDetermination.isPresent())
        {
            throw new DvaSopApiRuntimeException("Missing service determinations.");
        }

        return new ServiceDeterminationPair(relevantWarlikeDetermination.get(),relevantNonWarlikeDetermination.get());

    }




}
