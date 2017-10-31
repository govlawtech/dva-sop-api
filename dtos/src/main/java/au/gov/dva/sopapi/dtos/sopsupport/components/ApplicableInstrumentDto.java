package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateDeserializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonProperty("condition")
    private final String _condition;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("effectiveFromDate")
    private LocalDate _effectiveFromDate;

    @JsonProperty("standardOfProof")
    private StandardOfProof standardOfProof;

    @JsonCreator
    public ApplicableInstrumentDto(@JsonProperty("registerId") String _registerId,
                                   @JsonProperty("instrumentNumber") String _instrumentNumber,
                                   @JsonProperty("citation") String _citation,
                                   @JsonProperty("condition") String condition,

                                   @JsonProperty("effectiveFromDate")
                                           @JsonDeserialize(using = LocalDateDeserializer.class)
                                   LocalDate _effectiveFromDate,
                                   @JsonProperty("standardOfProof")StandardOfProof standardOfProof)
    {
        this._registerId = _registerId;
        this._instrumentNumber = _instrumentNumber;
        this._citation = _citation;
        this._effectiveFromDate = _effectiveFromDate;
        this._condition = condition;
        this.standardOfProof = standardOfProof;
    }

    @JsonIgnore
    public String getRegisterId() {
        return _registerId;
    }

    @JsonIgnore
    public String getInstrumentNumber() {
        return _instrumentNumber;
    }

    @JsonIgnore
    public String getCitation() {
        return _citation;
    }

    @JsonIgnore
    public LocalDate getEffectiveFromDate() {
        return _effectiveFromDate;
    }

    @JsonIgnore
    public StandardOfProof getStandardOfProof() {
        return standardOfProof;
    }

    @JsonIgnore
    public String getCondition() {return _condition;}
}
