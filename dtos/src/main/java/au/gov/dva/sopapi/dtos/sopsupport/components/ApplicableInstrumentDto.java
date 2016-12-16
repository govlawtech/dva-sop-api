package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;

public class ApplicableInstrumentDto {

    @JsonProperty("registerId")
    private final String _registerId;

    @JsonProperty("instrumentNumber")
    private final String _instrumentNumber;

    @JsonProperty("citation")
    private final String _citation;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("effectiveFromDate")
    private LocalDate _effectiveFromDate;

    @JsonCreator
    public ApplicableInstrumentDto(@JsonProperty("registerId") String _registerId,
                                   @JsonProperty("instrumentNumber") String _instrumentNumber,
                                   @JsonProperty("citation") String _citation,

                                   @JsonProperty("effectiveFromDate")
                                           @JsonDeserialize(using = LocalDateDeserializer.class)
                                   LocalDate _effectiveFromDate)
    {
        this._registerId = _registerId;
        this._instrumentNumber = _instrumentNumber;
        this._citation = _citation;
        this._effectiveFromDate = _effectiveFromDate;
    }
}
