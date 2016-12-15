package au.gov.dva.sopapi.dtos.sopsupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {
    @Override
    public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {

        String formattedValue = value.format(DateTimeFormatter.ISO_OFFSET_DATE);
        gen.writeString(formattedValue);
    }
}
