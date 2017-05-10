package au.gov.dva.sopapi.interfaces;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonSerializable {
    JsonNode toJson();
}
