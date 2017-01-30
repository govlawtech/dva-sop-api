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
import java.util.stream.Collectors;

class Seeds {

    public static void queueNewSopChanges(Repository repository) {
        try {
            assert(repository.getAllSops().isEmpty() && repository.getInstrumentChanges().isEmpty());
            String[] registerIdsOfInitialSops =  Resources.toString(Resources.getResource("initialsops.txt"), Charsets.UTF_8).split("\\r?\\n");
            ImmutableSet<InstrumentChange> newInstruments = Arrays.stream(registerIdsOfInitialSops).map(id -> new NewInstrument(id, OffsetDateTime.now()))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));
            repository.addInstrumentChanges(newInstruments);
        } catch (IOException e) {
            throw new InitialSeedingError(e);
        }

    }

    public static void addServiceDeterminations(Repository repository, RegisterClient registerClient)
    {
        try {
            assert(repository.getServiceDeterminations().isEmpty());
            String[] registerIdsOfInitialServiceDeterminations =  Resources.toString(Resources.getResource("initialservicedeterminations.txt"), Charsets.UTF_8).split("\\r?\\n");

            Arrays.stream(registerIdsOfInitialServiceDeterminations)
                    .forEach(id -> {
                        ServiceDetermination serviceDetermination = ServiceDeterminations.create(id,registerClient);
                        repository.addServiceDetermination(serviceDetermination);
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
