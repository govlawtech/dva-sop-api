package au.gov.dva.sopapi.sopref.data.updates;

import au.gov.dva.sopapi.interfaces.InstrumentChangeFactory;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.sopref.data.updates.changefactories.EmailSubscriptionInstrumentChangeFactory;
import com.google.common.collect.ImmutableList;

import java.time.OffsetDateTime;

public class AutoUpdate {
    public static void updateStorage(SoPLoader soPLoader, LegRegChangeDetector legRegChangeDetector)
    {
        // check for updates, write to storage
        // detect changes,
        // create changes
        // save to DB if do not exist already


        // load latest
        soPLoader.applyAll(30);
    }

    public static ImmutableList<InstrumentChange> getOrderedChangesSince(OffsetDateTime fromDate)
    {
//        InstrumentChangeFactory freshlyPublished = new EmailSubscriptionInstrumentChangeFactory(
//                new LegislationRegisterEmailClientImpl()
//
//
//        )

        return null;
    }
}
