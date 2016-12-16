package au.gov.dva.sopapi.dtos.sopsupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String stringValue = p.getValueAsString();

        String withAssumedTime = stringValue.matches("^[0-9]{4,4}-[0-9]{2,2}-[0-9]{2,2}Z$") ?
                stringValue.replaceFirst("Z","T00:00:00Z") : stringValue;

        OffsetDateTime parsed = OffsetDateTime.parse(withAssumedTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return parsed;
    }
}

