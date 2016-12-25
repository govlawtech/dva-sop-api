package au.gov.dva.sopapi.dtos.sopsupport.components;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ServiceHistoryDto {

    @JsonProperty(value = "serviceSummaryInformation", required = true)
    private ServiceSummaryInfoDto _serviceSummaryInfoDto;

    @JsonProperty(value = "serviceDetails", required = true)
    private List<ServiceDto> _serviceDetailsArray;

    public ServiceHistoryDto(@JsonProperty("serviceSummaryInformation") ServiceSummaryInfoDto _serviceSummaryInfoDto, @JsonProperty("serviceDetails") List<ServiceDto> _serviceDetailsArray) {
        this._serviceSummaryInfoDto = _serviceSummaryInfoDto;
        this._serviceDetailsArray = _serviceDetailsArray;
    }

    public ServiceSummaryInfoDto get_serviceSummaryInfoDto() {
        return _serviceSummaryInfoDto;
    }

    public List<ServiceDto> get_serviceDetailsArray() {
        return _serviceDetailsArray;
    }
}

