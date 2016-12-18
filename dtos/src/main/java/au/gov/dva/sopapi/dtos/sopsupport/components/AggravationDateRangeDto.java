package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.sopsupport.OffsetDateTimeDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.OffsetDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.OffsetDateTime;

public class AggravationDateRangeDto {

    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonProperty("aggravationRangeStartDate")
    private final OffsetDateTime _startDate;

    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonProperty("aggravationRangeEndDate")
    private final OffsetDateTime _endDate;

    @JsonCreator
    public AggravationDateRangeDto(

            @JsonProperty("aggravationRangeStartDate")
            @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
                    OffsetDateTime _startDate,

            @JsonProperty("aggravationRangeEndDate")
            @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
            OffsetDateTime _endDate) {
        this._startDate = _startDate;
        this._endDate = _endDate;
    }
}
