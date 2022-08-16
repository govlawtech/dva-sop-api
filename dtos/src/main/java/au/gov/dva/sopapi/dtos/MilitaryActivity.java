package au.gov.dva.sopapi.dtos;

import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateSerializer;
import au.gov.dva.sopapi.dtos.sopsupport.MilitaryOperationType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.util.Optional;

public class MilitaryActivity {

    @JsonProperty("name")
    private final String _name;

    @JsonProperty("startDate")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate _startDate;

    @JsonProperty("endDate")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private Optional<LocalDate> _endDate;

    @JsonProperty("type")
    private final MilitaryOperationType _type;

    @JsonProperty("legalSource")
    private final String _legalSource;

    @JsonCreator
    public MilitaryActivity(
            @JsonProperty("name") String name,
            @JsonProperty("startDate") @JsonDeserialize(using = LocalDateDeserializer.class) @JsonSerialize(using = LocalDateSerializer.class) LocalDate startDate,
            @JsonProperty("endDate") @JsonDeserialize(using = LocalDateDeserializer.class) @JsonSerialize(using = LocalDateSerializer.class) Optional<LocalDate> endDate,
            @JsonProperty("type") MilitaryOperationType type,
            @JsonProperty("legalSource") String legalSource
    ){
       _name = name;
       _startDate = startDate;
       _endDate = endDate;
       _type = type;
       _legalSource = legalSource;
    }
}
