package au.gov.dva.sopref.data;

import au.gov.dva.sopref.interfaces.Repository;
import au.gov.dva.sopref.interfaces.model.InstrumentChange;
import com.fasterxml.jackson.databind.JsonNode;

public class DebugRepository implements Repository {
    private static DebugRepository ourInstance = new DebugRepository();

    public static DebugRepository getInstance() {
        return ourInstance;
    }

    private DebugRepository() {
    }

    @Override
    public void saveSop(JsonNode jsonNode) {

    }

    @Override
    public void getSop(String registerId) {

    }

    @Override
    public Iterable<JsonNode> getAllSops() {
        return null;
    }

    @Override
    public Iterable<InstrumentChange> getInstrumentChanges() {
        return null;
    }
}
