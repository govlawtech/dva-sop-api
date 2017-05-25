package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.sopref.*;
import au.gov.dva.sopapi.dtos.sopref.DefinedTerm;
import au.gov.dva.sopapi.dtos.sopref.FactorDto;
import au.gov.dva.sopapi.dtos.sopref.Operation;
import au.gov.dva.sopapi.dtos.sopsupport.CaseTraceDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.*;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopsupport.processingrules.DeploymentImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.ServiceHistoryImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.ServiceImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoTransformations {

    public static FactorDto fromFactor(au.gov.dva.sopapi.interfaces.model.Factor factor) {
        return new FactorDto(factor.getParagraph(), factor.getText(),
                factor.getDefinedTerms().stream().map(t -> DtoTransformations.fromDefinedTerm(t)).collect(Collectors.toList()));

    }

    public static DefinedTerm fromDefinedTerm(au.gov.dva.sopapi.interfaces.model.DefinedTerm definedTerm) {
        return new DefinedTerm(definedTerm.getTerm(), definedTerm.getDefinition());
    }

    public static Operation fromOperation(au.gov.dva.sopapi.interfaces.model.Operation operation) {
        return new Operation(operation.getName(),
                formatDate(operation.getStartDate()),
                operation.getEndDate().isPresent() ? Optional.of(formatDate(operation.getEndDate().get())) : Optional.empty(),
                operation.getServiceType().toString());
    }

    public static SoPFactorsResponse fromSop(SoP sop,  IncidentType incidentType) {


        ImmutableList<au.gov.dva.sopapi.interfaces.model.Factor> factorsToInclude = (incidentType == IncidentType.Aggravation) ?
                sop.getAggravationFactors() : ((incidentType == IncidentType.Onset) ?
                sop.getOnsetFactors() : ImmutableList.of());

        List<FactorDto> factorDtos = factorsToInclude.stream().map(f -> DtoTransformations.fromFactor(f)).collect(Collectors.toList());

        String instrumentNumber = String.format("%d/%d", sop.getInstrumentNumber().getNumber(), sop.getInstrumentNumber().getYear());

        return new SoPFactorsResponse(sop.getRegisterId(), sop.getCitation(), instrumentNumber, factorDtos);

    }

    public static OperationsResponse buildOperationsResponseDto(ServiceDeterminationPair latestServiceDeterminations) {
        List<String> registerIds = latestServiceDeterminations.getBoth().stream()
                .map(sd -> sd.getRegisterId())
                .collect(Collectors.toList());

        List<Operation> operations = latestServiceDeterminations.getBoth().stream()
                .flatMap(sd -> sd.getOperations().stream())
                .sorted((o1, o2) -> o2.getStartDate().compareTo(o1.getStartDate()))
                .map(DtoTransformations::fromOperation)
                .collect(Collectors.toList());

        return new OperationsResponse(registerIds, operations);
    }

    public static FactorWithInferredResultDto fromFactorWithSatisfaction(FactorWithSatisfaction factorWithSatisfaction) {

        FactorDto factorDto = fromFactor(factorWithSatisfaction.getFactor());
        return new FactorWithInferredResultDto(
                factorDto.get_paragraph(),
                factorDto.get_text(),
                factorDto.get_definedTerms(),
                factorWithSatisfaction.isSatisfied()
        );
    }

    public static ServiceHistory serviceHistoryFromDto(ServiceHistoryDto serviceHistoryDto)
    {
        return new ServiceHistoryImpl(serviceHistoryDto.get_serviceSummaryInfoDto().get_originalHireDate(),
                serviceHistoryDto.get_serviceDetailsArray().stream().map(serviceDto -> serviceFromDto(serviceDto))
        .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableSet::copyOf)));
    }

    private static Deployment deploymentFromDeploymentDto(OperationalServiceDto operationalServiceDto)
    {
        return new DeploymentImpl(operationalServiceDto.get_description(),
                operationalServiceDto.get_startDate(),
                operationalServiceDto.get_endDate() != null ? Optional.of(operationalServiceDto.get_endDate()) : Optional.empty());

    }

    private static Service serviceFromDto(ServiceDto serviceDto)
    {
        return new ServiceImpl(serviceDto.get_serviceName(),
                serviceDto.get_serviceType(),
                serviceDto.get_rank(),
                serviceDto.get_startDate(),
                serviceDto.get_endDate() != null ? Optional.of(serviceDto.get_endDate()): Optional.empty(),
                serviceDto.get_operationalServiceDtos().stream().map(operationalServiceDto -> deploymentFromDeploymentDto(operationalServiceDto)).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf)));

    }

    public static CaseTraceDto caseTraceDtoFromCaseTrace(CaseTrace caseTrace)
    {
        return new CaseTraceDto(
                caseTrace.getApplicableStandardOfProof(),
                caseTrace.getRequiredCftsDays(),
                caseTrace.getActualCftsDays(),
                caseTrace.getRequiredOperationalDaysForRh(),
                caseTrace.getActualOperationalDays(),
                caseTrace.getRhFactors().stream().map(f -> fromFactor(f)).collect(Collectors.toList()),
                caseTrace.getBopFactors().stream().map(f -> fromFactor(f)).collect(Collectors.toList()),
                caseTrace.getReasonings(),
                caseTrace.getLoggingTraces());
    }


    private static String formatDate(OffsetDateTime offsetDateTime)
    {
        return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE);
    }
}
