package au.gov.dva.interfaces;

import au.gov.dva.interfaces.model.InstrumentChange;
import au.gov.dva.interfaces.model.ServiceDetermination;
import au.gov.dva.interfaces.model.SoP;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;

public interface Repository {
     void saveSop(SoP sop);
     Optional<SoP> getSop(String registerId);
     ImmutableSet<SoP> getAllSops();
     Iterable<InstrumentChange> getInstrumentChanges();
     void addServiceDetermination(ServiceDetermination serviceDetermination);
     ImmutableSet<ServiceDetermination> getServiceDeterminations();

}
