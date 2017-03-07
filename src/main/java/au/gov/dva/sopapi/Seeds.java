package au.gov.dva.sopapi;

import au.gov.dva.sopapi.exceptions.InitialSeedingError;
import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.sopref.data.ServiceDeterminations;
import au.gov.dva.sopapi.sopref.data.updates.types.NewInstrument;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Seeds {

    public static void queueNewSopChanges(Repository repository) {
       try {
            String[] registerIdsOfInitialSops =  Resources.toString(Resources.getResource("initialsops.txt"), Charsets.UTF_8).split("\\r?\\n");
            ImmutableSet<InstrumentChange> newInstruments = Arrays.stream(registerIdsOfInitialSops).map(id -> new NewInstrument(id, OffsetDateTime.now()))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));

            ImmutableSet<InstrumentChange> existingInstrumentChanges = repository.getInstrumentChanges();

            ImmutableSet<InstrumentChange> newInstrumentsNotAlreadyInRepo =
                    newInstruments.stream().filter(instrumentChange -> !existingInstrumentChanges.contains(instrumentChange))
                    .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableSet::copyOf));

            repository.addInstrumentChanges(newInstrumentsNotAlreadyInRepo);
        } catch (IOException e) {
            throw new InitialSeedingError(e);
        }

    }

    public static void addServiceDeterminations(Repository repository, RegisterClient registerClient)
    {
        try {
            ImmutableSet<String> existingServiceDeterminationIds = repository.getServiceDeterminations()
                    .stream()
                    .map(sd -> sd.getRegisterId())
                    .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableSet::copyOf));

            String[] registerIdsOfInitialServiceDeterminations =  Resources.toString(Resources.getResource("initialservicedeterminations.txt"), Charsets.UTF_8).split("\\r?\\n");

            Arrays.stream(registerIdsOfInitialServiceDeterminations)
                    .forEach(id -> {
                        if (!existingServiceDeterminationIds.contains(id))
                        {
                            ServiceDetermination serviceDetermination = ServiceDeterminations.create(id, registerClient);
                            repository.addServiceDetermination(serviceDetermination);
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
