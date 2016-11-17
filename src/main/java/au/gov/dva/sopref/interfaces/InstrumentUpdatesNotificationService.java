package au.gov.dva.sopref.interfaces;

import au.gov.dva.sopref.interfaces.model.InstrumentChange;

import java.time.LocalDate;

public interface InstrumentUpdatesNotificationService {
    Iterable<InstrumentChange> getChangesFrom(LocalDate date);
}
