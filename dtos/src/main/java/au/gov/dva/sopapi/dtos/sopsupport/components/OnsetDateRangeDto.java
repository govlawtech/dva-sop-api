package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.sopsupport.OffsetDateTimeDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.OffsetDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.OffsetDateTime;

public class OnsetDateRangeDto {


    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonProperty("onsetRangeStartDate")
    private final OffsetDateTime _startDate;

    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonProperty("onsetRangeEndDate")
    private final OffsetDateTime _endDate;

    @JsonCreator
    public OnsetDateRangeDto(
            @JsonDeserialize(using = OffsetDateTimeDeserializer.class) @JsonProperty("onsetRangeStartDate") OffsetDateTime _startDate,
            @JsonDeserialize(using = OffsetDateTimeDeserializer.class) @JsonProperty("onsetRangeEndDate")  OffsetDateTime _endDate) {
        this._startDate = _startDate;
        this._endDate = _endDate;
    }

    public OffsetDateTime get_startDate() {
        return _startDate;
    }
    public OffsetDateTime get_endDate() {
        return _endDate;
    }
}

