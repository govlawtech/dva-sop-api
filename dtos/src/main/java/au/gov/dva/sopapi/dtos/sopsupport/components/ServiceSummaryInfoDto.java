package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.sopsupport.OffsetDateTimeDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.OffsetDateTimeSerializer;
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

    public OffsetDateTime get_originalHireDate() {
        return _originalHireDate;
    }
}
