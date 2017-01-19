package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;

public interface InstrumentChangeFactory {
    ImmutableSet<InstrumentChange> getChanges();
}

