package au.gov.dva.sopref.data.servicedeterminations;

import au.gov.dva.interfaces.model.Operation;
import au.gov.dva.interfaces.model.ServiceDetermination;
import au.gov.dva.interfaces.model.ServiceType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


public class StoredServiceDetermination implements ServiceDetermination {


    private static final String WARLIKE = "warlike";
    private static final String NON_WARLIKE = "non-warlike";
    private final String registerId;
    private final String citation;
    private final LocalDate commencementDate;
    private final ImmutableList<Operation> operations;
    private final ServiceType serviceType;

    @Override
    public String getRegisterId() {
        return registerId;
    }

    @Override
    public String getCitation() {
        return citation;
    }

    @Override
    public LocalDate getCommencementDate() {
        return commencementDate;
    }

    @Override
    public ImmutableList<Operation> getOperations() {
        return operations;
    }

    @Override
    public ServiceType getServiceType() {
        return serviceType;
    }

    private static class Labels {
        public static final String REGISTER_ID = "registerId";
        public static final String CITATION = "citation";
        public static final String COMMENCEMENT_DATE = "commencementDate";
        public static final String OPERATIONS = "operations";
        public static final String SERVICE_TYPE = "serviceType";
    }

    public static JsonNode toJson(ServiceDetermination serviceDetermination) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode root = objectMapper.createObjectNode();
        root.put(Labels.REGISTER_ID, serviceDetermination.getRegisterId());
        root.put(Labels.CITATION, serviceDetermination.getCitation());
        root.put(Labels.COMMENCEMENT_DATE, serviceDetermination.getCommencementDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        root.put(Labels.SERVICE_TYPE,serviceTypeToStringValue(serviceDetermination.getServiceType()));

        ArrayNode arrayRoot = root.putArray(Labels.OPERATIONS);
        serviceDetermination.getOperations().forEach(operation -> arrayRoot.add(StoredOperation.toJson(operation)));
        return root;
    }

    public StoredServiceDetermination(@Nonnull String registerId,@Nonnull String citation, @Nonnull LocalDate commencementDate, @Nonnull ImmutableList<Operation> operations, @Nonnull ServiceType serviceType) {

        this.registerId = registerId;
        this.citation = citation;
        this.commencementDate = commencementDate;
        this.operations = operations;
        this.serviceType = serviceType;
    }

    public static ServiceDetermination fromJson(JsonNode jsonNode)
    {
        Iterator<JsonNode> operations =  jsonNode.findPath(Labels.OPERATIONS).elements();
        assert (jsonNode.isArray());
        List<Operation> children = new ArrayList<>();
        for (Iterator<JsonNode> it = operations; it.hasNext(); ) {
            JsonNode el = it.next();
            children.add(StoredOperation.fromJson(el));
        }

        return new StoredServiceDetermination(
                jsonNode.findValue(Labels.REGISTER_ID).asText(),
                jsonNode.findValue(Labels.CITATION).asText(),
                LocalDate.parse(jsonNode.findValue(Labels.COMMENCEMENT_DATE).asText()),
                ImmutableList.copyOf(children),
                serviceTypeFromStringValue(jsonNode.findValue(Labels.SERVICE_TYPE).asText())
        );
    }

    private static String serviceTypeToStringValue(ServiceType serviceType)
    {
        switch (serviceType)
        {
            case WARLIKE: return WARLIKE;
            case NON_WARLIKE: return NON_WARLIKE;
        }
        throw new IllegalArgumentException(String.format("Do not recognise operation type: %s", serviceType));
    }

    private static ServiceType serviceTypeFromStringValue(String stringValue)
    {
        if (stringValue.contentEquals(WARLIKE))
            return ServiceType.WARLIKE;
        if (stringValue.contentEquals(NON_WARLIKE))
            return ServiceType.NON_WARLIKE;

        throw new IllegalArgumentException(String.format("Do not recognise operation type: %s", stringValue));
    }


}
