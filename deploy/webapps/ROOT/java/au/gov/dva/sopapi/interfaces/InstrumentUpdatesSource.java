package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.InstrumentChange;

import java.time.LocalDate;

public interface InstrumentUpdatesSource {
    Iterable<InstrumentChange> getChangesFrom(LocalDate date);
}
