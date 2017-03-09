package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.ApplicableInstrumentDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.FactorWithInferredResultDto;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SopSupport {

    public static SopSupportResponseDto applyRules(SopSupportRequestDto sopSupportRequestDto, ImmutableSet<SoPPair> sopPairs, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        Condition condition = ConditionFactory.create(sopPairs, sopSupportRequestDto.get_conditionDto());
        ServiceHistory serviceHistory = DtoTransformations.serviceHistoryFromDto(sopSupportRequestDto.get_serviceHistoryDto());

        if (ProcessingRuleFunctions.conditionIsBeforeService(condition, serviceHistory)) {
            caseTrace.addTrace(String.format("Condition onset started on %s, before hire date of %s, therefore no SoP factors are applicable.", condition.getStartDate(), serviceHistory.getHireDate()));
            return SopSupportResponseDto.createEmpty();
        }

        SoP applicableSop = condition.getProcessingRule().getApplicableSop(condition, serviceHistory, isOperational);
        ApplicableInstrumentDto applicableInstrumentDto = new ApplicableInstrumentDto(applicableSop.getRegisterId(),
                StoredSop.formatInstrumentNumber(applicableSop.getInstrumentNumber()),
                applicableSop.getCitation(),
                applicableSop.getEffectiveFromDate());

        List<FactorWithSatisfaction> inferredFactors = condition.getProcessingRule().getSatisfiedFactors(condition, applicableSop, serviceHistory);
        List<FactorWithInferredResultDto> factorDtos = inferredFactors.stream().map(factorWithSatisfaction -> DtoTransformations.fromFactorWithSatisfaction(factorWithSatisfaction)).collect(Collectors.toList());
        return new SopSupportResponseDto(applicableInstrumentDto, factorDtos);
    }

}
