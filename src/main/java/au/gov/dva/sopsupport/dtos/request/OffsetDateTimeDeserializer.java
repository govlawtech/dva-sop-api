package au.gov.dva.sopsupport.dtos.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeDeserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String stringValue = p.getValueAsString();

        String withAssumedTime = stringValue.matches("^[0-9]{4,4}-[0-9]{2,2}-[0-9]{2,2}Z$") ?
                stringValue.replaceFirst("Z","T00:00:00Z") : stringValue;

        OffsetDateTime parsed = OffsetDateTime.parse(withAssumedTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return parsed;
    }
}
