package au.gov.dva.sopapi.dtos.sopsupport;

import au.gov.dva.sopapi.dtos.sopsupport.components.ApplicableInstrumentDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.FactorWithInferredResultDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class ResponseDto {

    @JsonProperty("applicableInstrument")
    private final ApplicableInstrumentDto _applicableInstrumentDto;

    @JsonProperty("factors")
    private List<FactorWithInferredResultDto> _factors;

    public ResponseDto(ApplicableInstrumentDto _applicableInstrumentDto, List<FactorWithInferredResultDto> _factors) {
        this._applicableInstrumentDto = _applicableInstrumentDto;
        this._factors = _factors;
    }

    public ApplicableInstrumentDto getApplicableInstrumentDto() {
        return _applicableInstrumentDto;
    }

    public ImmutableList<FactorWithInferredResultDto> getFactors() {
        return ImmutableList.copyOf(_factors);
    }
}
