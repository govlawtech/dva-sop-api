package au.gov.dva.sopref;

import au.gov.dva.sopref.dtos.OperationsResponseDto;
import au.gov.dva.sopref.interfaces.model.ServiceDetermination;
import au.gov.dva.sopref.interfaces.model.ServiceType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GetOperations {

    public static Optional<ServiceDetermination> getLatestServiceDetermination(ImmutableSet<ServiceDetermination> allServiceDeterminations, LocalDate commencementDate, ServiceType serviceType)
    {
        List<LocalDate> commencementDates =  allServiceDeterminations.stream().map(sd -> sd.getCommencementDate()).collect(Collectors.toList());

        assert(commencementDates.size() == commencementDates.stream().distinct().count());

        return allServiceDeterminations.stream()
                .filter(sd -> sd.getServiceType().equals(serviceType))
                .filter(sd -> sd.getCommencementDate().isAfter(commencementDate) || sd.getCommencementDate().isEqual(commencementDate))
                .sorted((o1, o2) -> o2.getCommencementDate().compareTo(o1.getCommencementDate()))
                .findFirst();
    }


    public static ImmutableSet<ServiceDetermination> getLatestDeterminationPair(ImmutableSet<ServiceDetermination> allServiceDeterminations, LocalDate localDate)
    {
        Optional<ServiceDetermination> relevantWarlikeDetermination = GetOperations.getLatestServiceDetermination(allServiceDeterminations,localDate,ServiceType.WARLIKE);

        Optional<ServiceDetermination> relevantNonWarlikeDetermination = GetOperations.getLatestServiceDetermination(allServiceDeterminations,localDate,ServiceType.NON_WARLIKE);

        List<ServiceDetermination> present = new ArrayList<>();
        if (relevantNonWarlikeDetermination.isPresent())
            present.add(relevantNonWarlikeDetermination.get());
        if (relevantWarlikeDetermination.isPresent())
            present.add(relevantWarlikeDetermination.get());

        return ImmutableSet.copyOf(present);


    }



}
