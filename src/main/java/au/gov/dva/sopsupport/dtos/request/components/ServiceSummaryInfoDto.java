package au.gov.dva.sopsupport.dtos.request.components;

import au.gov.dva.sopsupport.dtos.request.OffsetDateTimeDeserializer;
import au.gov.dva.sopsupport.dtos.request.OffsetDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.OffsetDateTime;




public class ServiceSummaryInfoDto {

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonProperty(value = "originalHireDate", required = true)
    private OffsetDateTime _originalHireDate;

    public ServiceSummaryInfoDto( @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
                                  @JsonProperty("originalHireDate")
                                          OffsetDateTime _originalHireDate) {
        this._originalHireDate = _originalHireDate;
    }
}
