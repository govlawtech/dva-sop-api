package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.util.List;

public class ServiceDto {

    @JsonProperty(value = "service", required = true)
    private final ServiceBranch _serviceName; // eg "Royal Australian Air Force

    @JsonProperty(value = "serviceType", required = true)
    private final EmploymentType _serviceType;  // eg "Regular/Permanent Force

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty(value = "startDate", required = true)
    private final LocalDate _startDate;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("endDate")
    private final LocalDate _endDate;

    @JsonProperty("rank")
    private final Rank _rank;

    @JsonProperty(value = "operationalService", required = true)
    private List<OperationalServiceDto> _operationalServiceDtos;

    @JsonCreator
    public ServiceDto(@JsonProperty("serviceName") ServiceBranch _serviceName,
                      @JsonProperty("serviceType") EmploymentType _employmentType,
                      @JsonProperty("startDate")
                      @JsonDeserialize(using = LocalDateDeserializer.class)
                              LocalDate _startDate,
                      @JsonProperty("endDate")
                      @JsonDeserialize(using = LocalDateDeserializer.class)
                      LocalDate _endDate,
                      @JsonProperty("rank")
                      Rank rank,
                      @JsonProperty("operationalService")
                      List<OperationalServiceDto> _operationalServiceDtos) {
        this._serviceName = _serviceName;
        this._serviceType = _employmentType;
        this._startDate = _startDate;
        this._endDate = _endDate;
        this._rank = rank;
        this._operationalServiceDtos = _operationalServiceDtos;
    }

    public ServiceBranch get_serviceName() {
        return _serviceName;
    }

    public EmploymentType get_serviceType() {
        return _serviceType;
    }

    public LocalDate get_startDate() {
        return _startDate;
    }

    public LocalDate get_endDate() {
        return _endDate;
    }

    public Rank get_rank() {
        return _rank;
    }

    public List<OperationalServiceDto> get_operationalServiceDtos() {
        return _operationalServiceDtos;
    }
}
