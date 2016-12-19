package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.*;
import au.gov.dva.sopapi.interfaces.model.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DtoTransformations {

    public static FactorDto fromFactor(Factor factor)
    {
        return new FactorDto(factor.getParagraph(),factor.getText(),
                factor.getDefinedTerms().stream().map(t -> DtoTransformations.fromDefinedTerm(t)).collect(Collectors.toList()));

    }

    public static DefinedTermDto fromDefinedTerm(DefinedTerm definedTerm)
    {
        return new DefinedTermDto(definedTerm.getTerm(),definedTerm.getDefinition());
    }

    public static OperationDto fromOperation(Operation operation)
    {
        return new OperationDto(operation.getName(),
                formatDate(operation.getStartDate()),
                operation.getEndDate().isPresent() ? Optional.of(formatDate(operation.getEndDate().get())) : Optional.empty(),
                operation.getServiceType().toString());
    }

    public static SoPDto fromSop(SoP sop, StandardOfProof standardOfProof, IncidentType incidentType) {


        ImmutableList<Factor> factorsToInclude = (sop.getStandardOfProof() != standardOfProof) ? ImmutableList.of() : (incidentType == IncidentType.Aggravation) ?
                sop.getAggravationFactors() : ((incidentType == IncidentType.Onset) ?
                sop.getOnsetFactors() : ImmutableList.of());

        List<FactorDto> factorDtos = factorsToInclude.stream().map(f -> DtoTransformations.fromFactor(f)).collect(Collectors.toList());

        String instrumentNumber = String.format("%d/%d", sop.getInstrumentNumber().getNumber(), sop.getInstrumentNumber().getYear());

        return new SoPDto(sop.getRegisterId(),sop.getCitation(),instrumentNumber,factorDtos);

    }

    public static OperationsResponseDto buildOperationsResponseDto(ImmutableSet<ServiceDetermination> latestServiceDeterminations)
    {

        List<String> registerIds = latestServiceDeterminations.stream()
                .map(sd -> sd.getRegisterId())
                .collect(Collectors.toList());

        List<OperationDto> operationDtos = latestServiceDeterminations.stream()
                .flatMap(sd -> sd.getOperations().stream())
                .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
                .map(DtoTransformations::fromOperation)
                .collect(Collectors.toList());

        return new OperationsResponseDto(registerIds,operationDtos);
    }


    private static String formatDate(OffsetDateTime offsetDateTime)
    {
        return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE);
    }
}
