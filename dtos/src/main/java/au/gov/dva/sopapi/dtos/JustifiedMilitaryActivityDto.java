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
import java.util.List;
import java.util.Optional;

public class JustifiedMilitaryActivityDto {
    @JsonProperty("name")
    private final String _name;

    @JsonProperty("startDate")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate _startDate;

    @JsonProperty("endDate")
    @JsonSerialize(using = LocalDateOptionalSerializer.class)
    private Optional<LocalDate> _endDate;

    @JsonProperty("type")
    private final MilitaryOperationType _type;

    @JsonProperty("legalSource")
    private final String _legalSource;

    @JsonProperty("deployments")
    private final List<DeploymentDto> _deployments;

    @JsonCreator
    public JustifiedMilitaryActivityDto(
            @JsonProperty("name") String name,
            @JsonProperty("startDate") @JsonSerialize(using = LocalDateSerializer.class) LocalDate startDate,
            @JsonProperty("endDate")  @JsonSerialize(using = LocalDateOptionalSerializer.class) Optional<LocalDate> endDate,
            @JsonProperty("type") MilitaryOperationType type,
            @JsonProperty("legalSource") String legalSource,
            @JsonProperty("deployments") List<DeploymentDto> deployments
    ){
        _name = name;
        _startDate = startDate;
        _endDate = endDate;
        _type = type;
        _legalSource = legalSource;
        _deployments = deployments;
    }
}
