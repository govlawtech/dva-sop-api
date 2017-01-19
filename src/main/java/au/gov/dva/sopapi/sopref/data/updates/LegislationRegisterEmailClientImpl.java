package au.gov.dva.sopapi.sopref.data.updates;

import au.gov.dva.sopapi.interfaces.LegislationRegisterEmailClient;
import au.gov.dva.sopapi.interfaces.model.LegislationRegisterEmailUpdate;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

public class LegislationRegisterEmailClientImpl implements LegislationRegisterEmailClient {
    @Override
    public CompletableFuture<ImmutableSet<LegislationRegisterEmailUpdate>> getUpdatesFrom(OffsetDateTime fromDate) {
        return LegislationRegisterEmailUpdates.getLatestAfter(fromDate);
    }
}
