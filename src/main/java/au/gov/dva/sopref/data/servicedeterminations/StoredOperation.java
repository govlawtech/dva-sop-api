package au.gov.dva.sopref.data.servicedeterminations;

import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.ServiceType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class StoredOperation implements Operation {

    @Nonnull
    private final String name;
    @Nonnull
    private final LocalDate startDate;
    private final Optional<LocalDate> endDate;
    private final ServiceType serviceType;

    public StoredOperation(@Nonnull String name, @Nonnull LocalDate startDate, Optional<LocalDate> endDate, ServiceType serviceType) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.serviceType = serviceType;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public ServiceType getServiceType() {
        return serviceType;
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
        objectNode.put(Labels.TYPE,operation.getServiceType().toString());
        return objectNode;
    }

    public static Operation fromJson(JsonNode jsonNode)
    {
        return new StoredOperation(
                jsonNode.findValue(Labels.NAME).asText(),
                LocalDate.parse(jsonNode.findValue(Labels.START_DATE).asText()),
                jsonNode.has(Labels.END_DATE) ? Optional.of(LocalDate.parse(jsonNode.findValue(Labels.END_DATE).asText())) : Optional.empty(),
                ServiceType.valueOf(jsonNode.findValue(Labels.TYPE).asText())
        );
    }
}
