package au.gov.dva.sopapi.sopref.data.updates;

import au.gov.dva.sopapi.exceptions.DvaSopApiError;
import au.gov.dva.sopapi.interfaces.InstrumentChangeFactory;
import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.SoPLoader;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.sopref.data.ServiceDeterminations;
import au.gov.dva.sopapi.sopref.parsing.ServiceLocator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

public class AutoUpdate {

    private static Logger logger = LoggerFactory.getLogger("dvasopapi.autoupdate");

    public static void patchSoPChanges(Repository repository) {

        try {
            logger.trace("Starting to patch SoP changes to repository.");
            SoPLoader soPLoader = new SoPLoaderImpl(
                    repository,
                    new FederalRegisterOfLegislationClient(),
                    s -> ServiceLocator.findTextCleanser(s),
                    s -> ServiceLocator.findSoPFactory(s)
            );
            soPLoader.applyAll(60);
        } catch (DvaSopApiError e) {
            logger.error("Error occurred when patching repository.", e);
        }
    }

    public static void updateChangeList(Repository repository, InstrumentChangeFactory newInstrumentFactory, InstrumentChangeFactory updatedInstrumentFactory) {

        try {
            logger.trace("Starting to check for SoP updates...");
            ImmutableSet<InstrumentChange> newInstruments = newInstrumentFactory.getChanges();
            logger.trace(String.format("Number of new instruments found: %d.", newInstruments.size()));
            ImmutableSet<InstrumentChange> updatedInstruments = updatedInstrumentFactory.getChanges();
            logger.trace(String.format("Number of instrument updates detected: %s.", updatedInstruments.size()));
            ImmutableSet<InstrumentChange> allNewChanges = new ImmutableSet.Builder<InstrumentChange>()
                    .addAll(newInstruments)
                    .addAll(updatedInstruments)
                    .build();

            ImmutableSet<InstrumentChange> existingChanges = repository.getInstrumentChanges();

            ImmutableSet<InstrumentChange> newChangesNotInRepository = Sets.difference(allNewChanges, existingChanges).immutableCopy();
            if (!newChangesNotInRepository.isEmpty()) {
                logger.trace(String.format("Instrument changes not already in repository: %s.",
                        String.join("\n", newChangesNotInRepository.stream().map(InstrumentChange::toString).collect(Collectors.toList()))));
                repository.addInstrumentChanges(newChangesNotInRepository);
                logger.trace(String.format("Added %s instrument changes to repository.", newChangesNotInRepository.size()));
            }
            repository.setLastUpdated(OffsetDateTime.now());
            logger.trace("Finished checking for SoP updates.");
        } catch (DvaSopApiError e) {
            logger.error("Error occurred when updating change list.", e);
        }

    }

    public static void updateServiceDeterminations(Repository repository, RegisterClient registerClient) {
        logger.trace("Starting to update Service Determinations...");
        LegRegChangeDetector legRegChangeDetector = new LegRegChangeDetector(registerClient);

        ImmutableSet<String> currentServiceDeterminationIds = repository.getServiceDeterminations().stream()
                .map(sd -> sd.getRegisterId())
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));

        ImmutableSet<InstrumentChange> replacements = legRegChangeDetector.detectReplacements(currentServiceDeterminationIds);
        if (!replacements.isEmpty()) {
            logger.trace(String.format("Service Determination replacements found: %s.", String.join("\n", replacements.stream().map(r -> r.toString()).collect(Collectors.toList()))));
            replacements.forEach(r -> {
                ServiceDetermination serviceDetermination = ServiceDeterminations.create(r.getTargetInstrumentId(), registerClient);
                repository.archiveServiceDetermination(r.getSourceInstrumentId());
                repository.addServiceDetermination(serviceDetermination);
            });
        }


    }


}




