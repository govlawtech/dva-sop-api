package au.gov.dva.sopref.interfaces;

import au.gov.dva.sopref.interfaces.model.InstrumentChange;
import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.ServiceDetermination;
import au.gov.dva.sopref.interfaces.model.SoP;
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
