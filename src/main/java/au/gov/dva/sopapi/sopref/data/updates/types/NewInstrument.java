package au.gov.dva.sopapi.sopref.data.updates.types;

import au.gov.dva.sopapi.exceptions.AutoUpdateError;
import au.gov.dva.sopapi.interfaces.JsonSerializable;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.InstrumentChangeBase;
import au.gov.dva.sopapi.interfaces.model.SoP;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Function;

public class NewInstrument extends InstrumentChangeBase implements InstrumentChange, JsonSerializable {

    public NewInstrument(String registerId, OffsetDateTime date) {
        super(registerId, registerId, date);
    }



    public static final String TYPE_NAME = "new";


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

    public JsonNode toJson()
    {

        ObjectNode root =  getCommonNode(TYPE_NAME,getDate());
        root.put("registerId",super.getSourceRegisterId());
        return root;
    }

    @Override
    public void apply(Repository repository, Function<String,Optional<SoP>> soPProvider)
    {
        Optional<SoP> existingIfAny = repository.getSop(this.getTargetInstrumentId());
        if (existingIfAny.isPresent())
            return;

        Optional<SoP> sop = soPProvider.apply(getTargetInstrumentId());
        if (!sop.isPresent())
        {
            throw new AutoUpdateError(String.format("Cannot get a SoP for instrument ID: %s", getTargetInstrumentId()));
        }

        repository.saveSop(sop.get());
    }


    @Override
    public String toString() {
        return "NewInstrument{} " + super.toString();
    }

    public static NewInstrument fromJson(JsonNode jsonNode)
    {
        return new NewInstrument(
                jsonNode.findValue("registerId").asText(),extractDate(jsonNode));
    }

}
