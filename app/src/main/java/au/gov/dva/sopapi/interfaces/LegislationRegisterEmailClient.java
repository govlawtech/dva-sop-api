package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.LegislationRegisterEmailUpdate;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

public interface LegislationRegisterEmailClient {
    CompletableFuture<ImmutableSet<LegislationRegisterEmailUpdate>> getUpdatesBetween(OffsetDateTime startDateExclusive, OffsetDateTime endDateExclusive);
}
