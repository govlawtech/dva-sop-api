package au.gov.dva.sopapi.sopref.data.updates.types;

import au.gov.dva.sopapi.exceptions.AutoUpdateError;
import au.gov.dva.sopapi.interfaces.JsonSerializable;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.InstrumentChangeBase;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Function;

public class Compilation extends InstrumentChangeBase implements InstrumentChange, JsonSerializable {


    public Compilation(String currentRegisterId, String newCompilationId, OffsetDateTime date) {
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
    public void apply(Repository repository, Function<String, Optional<SoP>> soPProvider) {

        Optional<SoP> existing = repository.getSop(getTargetRegisterId());
        if (existing.isPresent())
            return;

        Optional<SoP> toEndDate = repository.getSop(getSourceInstrumentId());
        if (!toEndDate.isPresent()) {
            throw new AutoUpdateError(String.format("Attempt to update the end date of SoP %s failed because it is not present in the Repository.", getSourceInstrumentId()));
        }

        Optional<SoP> newCompilation = soPProvider.apply(getTargetInstrumentId());
        if (!newCompilation.isPresent()) {
            throw new AutoUpdateError(String.format("Could not get new compilation for SoP: %s", getTargetInstrumentId()));
        }

        SoP endDatedSop = StoredSop.withEndDate(toEndDate.get(), newCompilation.get().getEffectiveFromDate().minusDays(1));
        repository.archiveSoP(getSourceInstrumentId());
        repository.saveSop(endDatedSop);
        repository.saveSop(newCompilation.get());
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

    public static Compilation fromJson(JsonNode jsonNode) {
        return new Compilation(jsonNode.findValue(SUPERSEDED_LABEL).asText(),
                jsonNode.findValue(COMPILATION_LABEL).asText(),
                extractDate(jsonNode)
        );
    }


}
