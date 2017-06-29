package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;

public class ServiceSummaryInfoDto {

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty(value = "originalHireDate", required = true)
    private LocalDate _originalHireDate;

    public ServiceSummaryInfoDto( @JsonDeserialize(using = LocalDateDeserializer.class)
                                  @JsonProperty("originalHireDate")
                                          LocalDate _originalHireDate) {
        this._originalHireDate = _originalHireDate;
    }

    public LocalDate get_originalHireDate() {
        return _originalHireDate;
    }
}
