package au.gov.dva.sopapi.sopref.data.servicedeterminations;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.interfaces.model.ServiceType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class StoredOperation implements Operation {

    private final String name;
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
    public String getName() {
        return name;
    }

    @Override
    public ServiceType getServiceType() {
        return serviceType;
    }


    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public Optional<LocalDate> getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        return "StoredOperation{" +
                "name='" + name + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", serviceType=" + serviceType +
                '}';
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
            objectNode.put(Labels.END_DATE, operation.getEndDate().get().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        objectNode.put(Labels.TYPE,operation.getServiceType().toString());
        return objectNode;
    }

    public static Operation fromJson(JsonNode jsonNode)
    {
        return new StoredOperation(
                jsonNode.findValue(Labels.NAME).asText(),

                DateTimeUtils.localDateStringToActMidnightOdt(jsonNode.findValue(Labels.START_DATE).asText()).toLocalDate(),

                        jsonNode.has(Labels.END_DATE) ? Optional.of(
                        DateTimeUtils.localDateToNextMidnightCanberraTime(jsonNode.findValue(Labels.END_DATE).asText()).toLocalDate()) : Optional.empty(),
                ServiceType.fromText(jsonNode.findValue(Labels.TYPE).asText())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        StoredOperation that = (StoredOperation) o;

        if (!this.name.equals(that.name)) return false;
        if (!this.startDate.equals(that.startDate)) return false;
        if (!this.endDate.equals(that.endDate)) return false;
        return this.serviceType == that.serviceType;
    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.startDate.hashCode();
        result = 31 * result + this.endDate.hashCode();
        result = 31 * result + this.serviceType.hashCode();
        return result;
    }
}
