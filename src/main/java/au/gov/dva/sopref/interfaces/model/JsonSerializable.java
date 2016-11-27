package au.gov.dva.sopref.interfaces.model;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonSerializable {
    JsonNode toJson();
}
