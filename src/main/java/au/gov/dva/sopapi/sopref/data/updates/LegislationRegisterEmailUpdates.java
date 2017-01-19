package au.gov.dva.sopapi.sopref.data.updates;

import au.gov.dva.sopapi.interfaces.model.LegislationRegisterEmailUpdate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

public class LegislationRegisterEmailUpdates {

    public static CompletableFuture<ImmutableSet<LegislationRegisterEmailUpdate>> getLatestAfter(OffsetDateTime offsetDateTime)
    {
        // todo
        return null;
    }
}
