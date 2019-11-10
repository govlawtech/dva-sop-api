package au.gov.dva.sopapi.dtos.sopsupport;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class StandardOfProofDeserializer extends JsonDeserializer<StandardOfProof> {
    @Override
    public StandardOfProof deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String stringValue = p.getValueAsString();
        return StandardOfProof.fromAbbreviation(stringValue);

    }
}
