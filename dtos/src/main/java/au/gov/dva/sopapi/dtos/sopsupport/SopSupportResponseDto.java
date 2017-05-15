package au.gov.dva.sopapi.dtos.sopsupport;


import au.gov.dva.sopapi.dtos.DvaSopApiDtoError;
import au.gov.dva.sopapi.dtos.sopsupport.components.ApplicableInstrumentDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.FactorWithInferredResultDto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;

public class SopSupportResponseDto {

    @JsonProperty("applicableInstrument")
    private final ApplicableInstrumentDto _applicableInstrumentDto;

    @JsonProperty("factors")
    private List<FactorWithInferredResultDto> _factors;

    @JsonProperty("caseTrace")
    private CaseTraceDto _caseTrace;

    @JsonCreator
    public SopSupportResponseDto(@JsonProperty("applicableInstrument") ApplicableInstrumentDto _applicableInstrumentDto,
                                 @JsonProperty("factors") List<FactorWithInferredResultDto> _factors,
                                 @JsonProperty("caseTrace") CaseTraceDto _caseTraceDto) {
        this._caseTrace = _caseTraceDto;
        this._applicableInstrumentDto = _applicableInstrumentDto;
        this._factors = _factors;
    }

    @JsonIgnore
    public CaseTraceDto getCaseTrace() {
        return _caseTrace;
    }

    @JsonIgnore
    public ApplicableInstrumentDto getApplicableInstrument() {
        return _applicableInstrumentDto;
    }

    @JsonIgnore
    public ImmutableList<FactorWithInferredResultDto> getFactors() {
        return ImmutableList.copyOf(_factors);
    }


    public static String toJsonString(SopSupportResponseDto sopSupportResponseDto) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        String jsonString = null;

        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter()
                    .with(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                    .writeValueAsString(sopSupportResponseDto);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiDtoError(e);
        }

        return jsonString;
    }

    public static SopSupportResponseDto fromJsonString(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        try {
            SopSupportResponseDto operationsResponseDto =
                    objectMapper.reader()
                            .with(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                            .forType(SopSupportResponseDto.class)
                            .readValue(jsonString);

            return operationsResponseDto;
        } catch (IOException e) {
            throw new DvaSopApiDtoError(e);
        }
    }
}



