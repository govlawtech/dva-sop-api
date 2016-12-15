package au.gov.dva.sopsupport.dtos.request.components;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public class ServiceHistoryDto {

    @JsonProperty(value = "serviceSummaryInformation", required = true)
    ServiceSummaryInfoDto _serviceSummaryInfoDto;

    @JsonProperty(value = "serviceDetails", required = true)
    List<ServiceDto> _serviceDetailsArray;

    public ServiceHistoryDto(@JsonProperty("serviceSummaryInformation") ServiceSummaryInfoDto _serviceSummaryInfoDto, @JsonProperty("serviceDetails") List<ServiceDto> _serviceDetailsArray) {
        this._serviceSummaryInfoDto = _serviceSummaryInfoDto;
        this._serviceDetailsArray = _serviceDetailsArray;
    }
}

