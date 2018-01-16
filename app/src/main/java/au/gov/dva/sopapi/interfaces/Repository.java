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
     Optional<byte[]> getSopPdf(String registerId);
     ImmutableSet<SoP> getAllSops();
     void archiveSoP(String registerId);
     ImmutableSet<InstrumentChange> getInstrumentChanges();
     ImmutableSet<InstrumentChange> getRetryQueue();
     void addToRetryQueue(InstrumentChange instrumentChange);
     void addInstrumentChanges(ImmutableSet<InstrumentChange> instrumentChanges);
     void purgeInstrumentChanges();
     void addServiceDetermination(ServiceDetermination serviceDetermination);
     void archiveServiceDetermination(String registerId);
     ImmutableSet<ServiceDetermination> getServiceDeterminations();
     Optional<OffsetDateTime> getLastUpdated();
     void setLastUpdated(OffsetDateTime offsetDateTime);
     Optional<RuleConfigurationRepository> getRuleConfigurationRepository();
     void setRulesConfig(byte[] rhCsv, byte[] bopCsv);
     Optional<String> getSocfServiceRegionsYaml();
     void purge();
}
