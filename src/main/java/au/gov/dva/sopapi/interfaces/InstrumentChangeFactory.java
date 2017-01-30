package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import com.google.common.collect.ImmutableSet;

public interface InstrumentChangeFactory {
    ImmutableSet<InstrumentChange> getChanges();
}

