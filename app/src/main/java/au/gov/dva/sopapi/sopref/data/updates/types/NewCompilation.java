package au.gov.dva.sopapi.sopref.data.updates.types;

import au.gov.dva.sopapi.interfaces.JsonSerializable;
import au.gov.dva.sopapi.interfaces.model.InstrumentChangeBase;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.OffsetDateTime;

public class NewCompilation extends InstrumentChangeBase implements InstrumentChange, JsonSerializable {


    public NewCompilation(String currentRegisterId, String newCompilationId, OffsetDateTime date) {
        super(currentRegisterId, newCompilationId, date);
    }

    public static final String TYPE_NAME = "compilation";
    private static final String SUPERSEDED_LABEL = "supersededRegisterId";
    private static final String COMPILATION_LABEL = "compilationRegisterId";

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
    public JsonNode toJson() {
        ObjectNode root = getCommonNode(TYPE_NAME, getDate());
        root.put(SUPERSEDED_LABEL, this.getSourceInstrumentId());
        root.put(COMPILATION_LABEL, this.getTargetInstrumentId());
        return root;
    }

    @Override
    public String toString() {
        return "Compilation{} " + super.toString();
    }

    public static NewCompilation fromJson(JsonNode jsonNode) {
        return new NewCompilation(jsonNode.findValue(SUPERSEDED_LABEL).asText(),
                jsonNode.findValue(COMPILATION_LABEL).asText(),
                extractDate(jsonNode)
        );
    }


}
