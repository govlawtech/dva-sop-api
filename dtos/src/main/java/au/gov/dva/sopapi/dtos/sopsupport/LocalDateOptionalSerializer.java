package au.gov.dva.sopapi.dtos.sopsupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LocalDateOptionalSerializer extends JsonSerializer<Optional<LocalDate>>
{
    @Override
    public void serialize(Optional<LocalDate> value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        {
            gen.writeString(value.get().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        else {
            gen.writeNull();
        }
    }
}