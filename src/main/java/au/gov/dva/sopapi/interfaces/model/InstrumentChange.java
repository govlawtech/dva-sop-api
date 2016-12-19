package au.gov.dva.sopapi.interfaces.model;

import java.time.LocalDate;

public interface InstrumentChange {
     String getInstrumentId();
     InstrumentChangeType getInstrumentChangeType();
     LocalDate getDate();
}

