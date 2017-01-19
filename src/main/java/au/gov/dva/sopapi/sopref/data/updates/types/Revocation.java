package au.gov.dva.sopapi.sopref.data.updates.types;

import au.gov.dva.sopapi.interfaces.JsonSerializable;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.InstrumentChangeBase;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Function;

public class Revocation extends InstrumentChangeBase implements InstrumentChange, JsonSerializable {
    private final LocalDate revocationDate;

    @Override
    public String toString() {
        return "Revocation{" +
                "revocationDate=" + revocationDate +
                "} " + super.toString();
    }

    public Revocation(String registerId, OffsetDateTime date, LocalDate revocationDate) {
        super(registerId, registerId, date);
        this.revocationDate = revocationDate;
    }

    public static final String TYPE_NAME = "revocation";



    @Override
    public OffsetDateTime getDate() {
        return super.getDate();
    }

    @Override
    public String getSourceInstrumentId() {
        return super.getSourceRegisterId();
    }

    @Override
    public String getTargetInstrumentId() {
        return super.getTargetRegisterId();
    }

    @Override
    public void apply(Repository repository, Function<String, Optional<SoP>> soPProvider) {

        Optional<SoP> existing = repository.getSop(getSourceInstrumentId());
        if (!existing.isPresent())
            return;

        repository.archiveSoP(getSourceRegisterId());
        SoP endDated = StoredSop.withEndDate(existing.get(), revocationDate);
        repository.saveSop(endDated);
    }

    private static final String REVOCATION_DATE = "revocationDate";
    private static final String REGISTER_ID = "registerId";

    @Override
    public JsonNode toJson() {
        ObjectNode root = getCommonNode(TYPE_NAME,getDate());
        root.put(REVOCATION_DATE,revocationDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        root.put(REGISTER_ID,getSourceInstrumentId());
        return root;
    }

    public static Revocation fromJson(JsonNode jsonNode)
    {
        String repealDateString =  jsonNode.findValue(REVOCATION_DATE).asText();
        LocalDate repealDate = LocalDate.parse(repealDateString,DateTimeFormatter.ISO_LOCAL_DATE);
        return new Revocation(jsonNode.findValue(REGISTER_ID).asText(),extractDate(jsonNode), repealDate);
    }
}
