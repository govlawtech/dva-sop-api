package au.gov.dva.sopapi.dtos;

import au.gov.dva.sopapi.dtos.sopsupport.LocalDateOptionalSerializer;
import au.gov.dva.sopapi.dtos.sopsupport.LocalDateSerializer;
import au.gov.dva.sopapi.dtos.sopsupport.components.MetadataKvpDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.OperationTypeCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.util.Optional;

public class DeploymentDto {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("startDate")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate startDate;
    @JsonProperty("endDate")
    @JsonSerialize(using = LocalDateOptionalSerializer.class)
    private final Optional<LocalDate> endDate;

    @JsonProperty(value = "operationTypeCode",required = false)
    private final OperationTypeCode[] operationTypeCodes;

    @JsonProperty(value = "metadata",required = false)
    private MetadataKvpDto[] metadata;

    @JsonCreator
    public DeploymentDto(@JsonProperty("name") String name, @JsonProperty("startDate")  LocalDate startDate, @JsonProperty("endDate") Optional<LocalDate> endDate, @JsonProperty("operationTypeCode") OperationTypeCode[] operationTypeCodes, @JsonProperty("metadata") MetadataKvpDto[] metadata)
    {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.operationTypeCodes = operationTypeCodes;
        this.metadata = metadata;

    }
}
