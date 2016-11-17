package au.gov.dva.sopref.interfaces.model;

import java.time.LocalDate;

public interface InstrumentChange {
     String getInstrumentId();
     InstrumentChangeType getInstrumentChangeType();
     LocalDate getDate();
}

