package au.gov.dva.interfaces;

import au.gov.dva.interfaces.model.InstrumentChange;

import java.time.LocalDate;

public interface InstrumentUpdatesNotificationService {
    Iterable<InstrumentChange> getChangesFrom(LocalDate date);
}
