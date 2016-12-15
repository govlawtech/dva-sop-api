package au.gov.dva.sopsupport.dtos.request.components;

import au.gov.dva.interfaces.model.Rank;
import au.gov.dva.sopsupport.dtos.request.OffsetDateTimeDeserializer;
import au.gov.dva.sopsupport.dtos.request.OffsetDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.OffsetDateTime;
import java.util.List;

public class ServiceDto {

    @JsonProperty(value = "service", required = true)
    private final String _serviceName; // eg "Royal Australian Air Force

    @JsonProperty(value = "serviceType", required = true)
    private final String _serviceType;  // eg "Regular/Permanent Force

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonProperty(value = "startDate", required = true)
    private final OffsetDateTime _startDate;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonProperty("endDate")
    private final OffsetDateTime _endDate;

    @JsonProperty("rank")
    private final Rank _rank;

    @JsonProperty(value = "operationalService", required = true)
    List<OperationalServiceDto> _operationalServiceDtos;

    @JsonCreator
    public ServiceDto(@JsonProperty("serviceName") String _serviceName,
                      @JsonProperty("serviceType") String _serviceType,
                      @JsonProperty("startDate")
                      @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
                              OffsetDateTime _startDate,
                      @JsonProperty("endDate")
                      @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
                      OffsetDateTime _endDate,
                      @JsonProperty("rank")
                      Rank rank,
                      @JsonProperty("operationalService")
                      List<OperationalServiceDto> _operationalServiceDtos) {
        this._serviceName = _serviceName;
        this._serviceType = _serviceType;
        this._startDate = _startDate;
        this._endDate = _endDate;
        this._rank = rank;
        this._operationalServiceDtos = _operationalServiceDtos;
    }
}
