package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.util.List;

public class OperationalServiceDto {

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("assigned")
    private final LocalDate _assigned;


    @JsonProperty(value = "description", required = true)
    private final String _description; // eg Operation Warden

    @JsonProperty(value = "event", required = true)
    private final String _event; // eg Within Specified Area

    @JsonProperty(value = "operationTypeCode", required = false)
    private final OperationTypeCode[] _operationTypeCodes;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty(value = "startDate", required = true)
    private final LocalDate _startDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("endDate")
    private final LocalDate _endDate;

    @JsonProperty(value = "metadata", required = false)
    private final MetadataKvpDto[] _metadata;

    @JsonCreator
    public OperationalServiceDto(

            @JsonProperty("assigned")
            @JsonDeserialize(using = LocalDateDeserializer.class)
            LocalDate _assigned,

            @JsonProperty("description")
            String _description,

            @JsonProperty("event")
            String _event,

            @JsonProperty("operationTypeCode")
            OperationTypeCode[] _operationTypeCodes,

            @JsonProperty("startDate")
            @JsonDeserialize(using = LocalDateDeserializer.class)
            LocalDate _startDate,

            @JsonProperty("endDate")
            @JsonDeserialize(using = LocalDateDeserializer.class)
            LocalDate _endDate,

            @JsonProperty("metadata")
            MetadataKvpDto[] _metadata)

    {
        this._assigned = _assigned;
        this._description = _description;
        this._event = _event;
        this._startDate = _startDate;
        this._endDate = _endDate;
        this._operationTypeCodes = _operationTypeCodes;
        this._metadata = _metadata;
    }

    public LocalDate get_assigned() {
        return _assigned;
    }

    public String get_description() {
        return _description;
    }

    public String get_event() {
        return _event;
    }

    public LocalDate get_startDate() {
        return _startDate;
    }

    public LocalDate get_endDate() {
        return _endDate;
    }

    public OperationTypeCode[] get_operationTypeCodes() {
        return _operationTypeCodes;
    }

    public MetadataKvpDto[] get_metadata() {
        return _metadata;
    }
}
