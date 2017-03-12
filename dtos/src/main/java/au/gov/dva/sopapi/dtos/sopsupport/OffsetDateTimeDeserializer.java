package au.gov.dva.sopapi.dtos.sopsupport;

import au.gov.dva.sopapi.DateTimeUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;

public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String stringValue = p.getValueAsString();

        OffsetDateTime parsed = DateTimeUtils.stringToOffsetDateTimeWithAssumptions(stringValue);

        return parsed;
    }
}

