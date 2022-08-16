package au.gov.dva.sopapi.dtos;

import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateOptionalSerializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateSerializer;
import au.gov.dva.sopapi.dtos.sopsupport.MilitaryOperationType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.util.Optional;

public class MilitaryActivity {

    private final String _name;

    private final LocalDate _startDate;

    private Optional<LocalDate> _endDate;

    private final MilitaryOperationType _type;

    private final String _legalSource;

    public MilitaryActivity(
            String name,
            LocalDate startDate,
            Optional<LocalDate> endDate,
            MilitaryOperationType type,
            String legalSource
    ){
       _name = name;
       _startDate = startDate;
       _endDate = endDate;
       _type = type;
       _legalSource = legalSource;
    }

    public MilitaryActivityDto toDto() {
        return new MilitaryActivityDto(
                _name,
                _startDate,
                _endDate.isPresent() ? _endDate.get() : null,
                _type,
                _legalSource
        );
    }
}
