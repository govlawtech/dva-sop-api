package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.LegislationRegisterEmailUpdate;
import au.gov.dva.sopapi.sopref.data.updates.LegislationRegisterEmailUpdates;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

public interface LegislationRegisterEmailClient {
    CompletableFuture<ImmutableSet<LegislationRegisterEmailUpdate>> getUpdatesFrom(OffsetDateTime fromDate);
}
