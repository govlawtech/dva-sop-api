package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface Repository {
     void addSop(SoP sop);
     Optional<SoP> getSop(String registerId);
     ImmutableSet<SoP> getAllSops();
     void archiveSoP(String registerId);
     ImmutableSet<InstrumentChange> getInstrumentChanges();
     void addInstrumentChanges(ImmutableSet<InstrumentChange> instrumentChanges);
     void addServiceDetermination(ServiceDetermination serviceDetermination);
     void archiveServiceDetermination(String registerId);
     ImmutableSet<ServiceDetermination> getServiceDeterminations();
     Optional<OffsetDateTime> getLastUpdated();
     void setLastUpdated(OffsetDateTime offsetDateTime);
     Optional<RuleConfigurationRepository> getRuleConfigurationRepository();
     void setRulesConfig(byte[] rhCsv, byte[] bopCsv);
     void purge();
}
