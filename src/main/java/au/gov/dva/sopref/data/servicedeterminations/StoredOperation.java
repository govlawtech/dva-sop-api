package au.gov.dva.sopref.data.servicedeterminations;

import au.gov.dva.sopref.interfaces.model.JsonSerializable;
import au.gov.dva.sopref.interfaces.model.Operation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class StoredOperation implements Operation, JsonSerializable {

    @Nonnull
    private final String name;
    @Nonnull
    private final LocalDate startDate;
    private final Optional<LocalDate> endDate;

    public StoredOperation(@Nonnull String name, @Nonnull LocalDate startDate, Optional<LocalDate> endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public Optional<LocalDate> getEndDate() {
        return endDate;
    }

    @Override
    public JsonNode toJson() {
        return toJson(this);
    }

    private static class Labels {
        public static final String NAME = "name";
        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";
        public static final String TYPE = "type";
    }

    public static JsonNode toJson(Operation operation) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put(Labels.NAME, operation.getName());
        objectNode.put(Labels.START_DATE, operation.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (operation.getEndDate().isPresent())
            objectNode.put(Labels.END_DATE, operation.getEndDate().get().format(DateTimeFormatter.ISO_LOCAL_DATE));
        return objectNode;
    }

    public static Operation fromJson(JsonNode jsonNode)
    {
        return new StoredOperation(
                jsonNode.findValue(Labels.NAME).asText(),
                LocalDate.parse(jsonNode.findValue(Labels.START_DATE).asText()),
                jsonNode.has(Labels.END_DATE) ? Optional.of(LocalDate.parse(jsonNode.findValue(Labels.END_DATE).asText())) : Optional.empty()
        );
    }
}
