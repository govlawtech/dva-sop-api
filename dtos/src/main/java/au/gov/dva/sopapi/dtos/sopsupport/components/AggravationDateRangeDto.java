package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;

public class AggravationDateRangeDto {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("aggravationRangeStartDate")
    private final LocalDate _startDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("aggravationRangeEndDate")
    private final LocalDate _endDate;

    @JsonCreator
    public AggravationDateRangeDto(

            @JsonProperty("aggravationRangeStartDate")
            @JsonDeserialize(using = LocalDateDeserializer.class)
                    LocalDate _startDate,

            @JsonProperty("aggravationRangeEndDate")
            @JsonDeserialize(using = LocalDateDeserializer.class)
            LocalDate _endDate) {
        this._startDate = _startDate;
        this._endDate = _endDate;
    }
}
