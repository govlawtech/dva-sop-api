package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.dtos.DeploymentDto;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.JustifiedMilitaryActivityDto;
import au.gov.dva.sopapi.dtos.sopref.*;
import au.gov.dva.sopapi.dtos.sopref.DefinedTerm;
import au.gov.dva.sopapi.dtos.sopref.FactorDto;
import au.gov.dva.sopapi.dtos.sopref.Operation;
import au.gov.dva.sopapi.dtos.sopsupport.CaseTraceDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.*;
import au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance.FactorLinkDto;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.ServiceDeterminationPair;
import au.gov.dva.sopapi.sopsupport.processingrules.CharacterisedDeploymentImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.DeploymentImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.ServiceHistoryImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.ServiceImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DtoTransformations {

    public static FactorDto fromFactor(Factor factor) {
        return new FactorDto(factor.getParagraph(), factor.getText(),
                factor.getDefinedTerms().stream().map(t -> DtoTransformations.fromDefinedTerm(t)).collect(Collectors.toList()));

    }

    public static FactorLinkDto fromFactorToLink(Factor factor) {

        String conditionVariantName = factor.getConditionVariant().isPresent() ? factor.getConditionVariant().get().getName() : null;
        return new FactorLinkDto(factor.getParagraph(), factor.getText(),
                factor.getDefinedTerms().stream().map(t -> DtoTransformations.fromDefinedTerm(t)).collect(Collectors.toList()),conditionVariantName);


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


        ImmutableList<Factor> factorsToInclude = (incidentType == IncidentType.Aggravation) ?
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
        OperationTypeCode[] operationTypeCodes = operationalServiceDto.get_operationTypeCodes() != null ? operationalServiceDto.get_operationTypeCodes() : new OperationTypeCode[0];
        MetadataKvpDto[] metadataKvpDtos = operationalServiceDto.get_metadata() != null ? operationalServiceDto.get_metadata() : new MetadataKvpDto[0];

        return new CharacterisedDeploymentImpl(
                    new DeploymentImpl(
                        operationalServiceDto.get_description(),
                        operationalServiceDto.get_startDate(),
                        operationalServiceDto.get_endDate() != null ? Optional.of(operationalServiceDto.get_endDate()) : Optional.empty(),
                        operationalServiceDto.get_event()),
                    operationTypeCodes,
                    metadataKvpDtos
                );

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

    private static DeploymentDto deploymentToDto(Deployment deployment){

        if (deployment instanceof CharacterisedDeployment)
        {
            CharacterisedDeployment characterisedDeployment = (CharacterisedDeployment) deployment;
            return new DeploymentDto(
                    characterisedDeployment.getOperationName(),
                    characterisedDeployment.getStartDate(),
                    characterisedDeployment.getEndDate(),
                    characterisedDeployment.getOperationTypeCodes(),
                    characterisedDeployment.getMetadata()
            );
        }
        else
        {
            return new DeploymentDto(
                    deployment.getOperationName(),
                    deployment.getStartDate(),
                    deployment.getEndDate(),
                    new OperationTypeCode[0],
                    new MetadataKvpDto[0]
            );
        }
    }

    private static JustifiedMilitaryActivityDto justifiedMilitaryActivityToDto(JustifiedMilitaryActivity justifiedMilitaryActivity)
    {
        return new JustifiedMilitaryActivityDto(
                justifiedMilitaryActivity.getMilitaryActivity().getName(),
                justifiedMilitaryActivity.getMilitaryActivity().getStartDate(),
                justifiedMilitaryActivity.getMilitaryActivity().getEndDate(),
                justifiedMilitaryActivity.getMilitaryActivity().getMilitaryOperationType(),
                justifiedMilitaryActivity.getMilitaryActivity().getLegalSource(),
                justifiedMilitaryActivity.getRelevantDeployments().stream().map(d -> deploymentToDto(d)).collect(Collectors.toList())
        );
    }
    public static CaseTraceDto caseTraceDtoFromCaseTrace(CaseTrace caseTrace)
    {
        return new CaseTraceDto(
                caseTrace.getConditionName(),
                caseTrace.getApplicableStandardOfProof(),
                caseTrace.getRequiredCftsDays(),
                caseTrace.getRequiredCftsDaysForRh(),
                caseTrace.getRequiredCftsDaysForBop(),
                caseTrace.getActualCftsDays(),
                caseTrace.getRequiredOperationalDaysForRh(),
                caseTrace.getActualOperationalDays(),
                caseTrace.getRhFactors().stream().map(f -> fromFactor(f)).collect(Collectors.toList()),
                caseTrace.getBopFactors().stream().map(f -> fromFactor(f)).collect(Collectors.toList()),
                caseTrace.getReasonings(),
                caseTrace.getRelevantOperations().stream().map(ma -> justifiedMilitaryActivityToDto(ma)).collect(Collectors.toList()),
                caseTrace.getLoggingTraces());
    }


    private static String formatDate(LocalDate localDate)
    {
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
