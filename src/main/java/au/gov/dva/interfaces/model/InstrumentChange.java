package au.gov.dva.interfaces.model;

import java.time.LocalDate;

public interface InstrumentChange {
     String getInstrumentId();
     InstrumentChangeType getInstrumentChangeType();
     LocalDate getDate();
}

