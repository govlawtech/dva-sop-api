package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;

public class OnsetDateRangeDto {


    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("onsetRangeStartDate")
    private final LocalDate _startDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("onsetRangeEndDate")
    private final LocalDate _endDate;

    @JsonCreator
    public OnsetDateRangeDto(
            @JsonDeserialize(using = LocalDateDeserializer.class) @JsonProperty("onsetRangeStartDate") LocalDate _startDate,
            @JsonDeserialize(using = LocalDateDeserializer.class) @JsonProperty("onsetRangeEndDate")  LocalDate _endDate) {
        this._startDate = _startDate;
        this._endDate = _endDate;
    }

    public LocalDate get_startDate() {
        return _startDate;
    }
    public LocalDate get_endDate() {
        return _endDate;
    }
}

