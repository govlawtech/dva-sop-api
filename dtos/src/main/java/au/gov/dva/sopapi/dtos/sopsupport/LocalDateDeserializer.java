package au.gov.dva.sopapi.dtos.sopsupport;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;


public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String stringValue = p.getValueAsString();
        LocalDate parsed = LocalDate.parse(stringValue);
        return parsed;
    }
}



