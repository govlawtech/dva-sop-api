package au.gov.dva.sopref.dtos;

import au.gov.dva.interfaces.model.Operation;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OperationDto {

    @JsonProperty("operationName")
    private final String _name;

    @JsonProperty("startDate")
    private final String _startDate;

    @JsonProperty("endDate")
    private String _endDate;

    @JsonProperty("type")
    private final String _type;

    public OperationDto(String _name, String _startDate, Optional<String> _endDate, String _type) {
        this._name = _name;
        this._startDate = _startDate;
        if (_endDate.isPresent())
            this._endDate = _endDate.get();
        this._type = _type;
    }

    public static OperationDto fromOperation(Operation operation)
    {
        return new OperationDto(operation.getName(),
                formatDate(operation.getStartDate()),
                operation.getEndDate().isPresent() ? Optional.of(formatDate(operation.getEndDate().get())) : Optional.empty(),
                operation.getServiceType().toString());
    }

    private static String formatDate(LocalDate localDate)
    {
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
