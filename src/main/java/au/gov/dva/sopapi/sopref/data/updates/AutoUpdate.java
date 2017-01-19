package au.gov.dva.sopapi.sopref.data.updates;

import au.gov.dva.sopapi.interfaces.InstrumentChangeFactory;

public class AutoUpdate {
    public static void updateStorage(SoPLoader soPLoader, InstrumentChangeFactory newInstruments, InstrumentChangeFactory changedInstruments)
    {
        // check for updates, write to storage

        

        // detect changes,
        // create changes
        // save to DB if do not exist already


        // load latest
        soPLoader.applyAll(30);

    }
}
