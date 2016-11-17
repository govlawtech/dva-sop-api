package au.gov.dva.sopref.interfaces;

import au.gov.dva.sopref.interfaces.model.InstrumentChange;
import com.fasterxml.jackson.databind.JsonNode;

public interface Repository {
     void saveSop(JsonNode jsonNode);
     void getSop(String registerId);
     Iterable<JsonNode> getAllSops();
     Iterable<InstrumentChange> getInstrumentChanges();
}
