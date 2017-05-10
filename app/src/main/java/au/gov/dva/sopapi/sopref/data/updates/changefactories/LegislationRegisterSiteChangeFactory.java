package au.gov.dva.sopapi.sopref.data.updates.changefactories;

import au.gov.dva.sopapi.interfaces.InstrumentChangeFactory;
import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.sopref.data.updates.LegRegChangeDetector;
import com.google.common.collect.ImmutableSet;

import java.util.function.Supplier;

public class LegislationRegisterSiteChangeFactory implements InstrumentChangeFactory {

    private final LegRegChangeDetector legRegChangeDetector;
    private final Supplier<ImmutableSet<String>> getExistingInstrumentIds;

    public LegislationRegisterSiteChangeFactory(RegisterClient registerClient,  Supplier<ImmutableSet<String>> getExistingInstrumentIds)
    {
        legRegChangeDetector = new LegRegChangeDetector(registerClient);
        this.getExistingInstrumentIds = getExistingInstrumentIds;
    }

    @Override
    public ImmutableSet<InstrumentChange> getChanges() {
        ImmutableSet<String> currentRegisterIds = getExistingInstrumentIds.get();
        ImmutableSet<InstrumentChange> newReplacements = legRegChangeDetector.detectReplacements(currentRegisterIds);
        ImmutableSet<InstrumentChange> newCompilations = legRegChangeDetector.detectNewCompilations(currentRegisterIds);
        return new ImmutableSet.Builder<InstrumentChange>().addAll(newReplacements).addAll(newCompilations).build();

    }
}
