package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.ApplicableInstrumentDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.FactorWithInferredResultDto;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import au.gov.dva.sopapi.sopsupport.processingrules.RulesResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SopSupport {



    public static RulesResult applyRules(RuleConfigurationRepository ruleConfigurationRepository, SopSupportRequestDto sopSupportRequestDto, ImmutableSet<SoPPair> sopPairs, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        Optional<Condition> conditionOptional = ConditionFactory.create(sopPairs, sopSupportRequestDto.get_conditionDto(),ruleConfigurationRepository);
        if (!conditionOptional.isPresent())
        {
            return RulesResult.createEmpty();
        }
        Condition condition = conditionOptional.get();
        ServiceHistory serviceHistory = DtoTransformations.serviceHistoryFromDto(sopSupportRequestDto.get_serviceHistoryDto());

        if (ProcessingRuleFunctions.conditionIsBeforeService(condition, serviceHistory)) {
            caseTrace.addTrace(String.format("Condition onset started on %s, before hire date of %s, therefore no SoP factors are applicable.", condition.getStartDate(), serviceHistory.getHireDate()));
            return  RulesResult.createEmpty();
        }

        if (sopSupportRequestDto.get_conditionDto().get_incidentType() == IncidentType.Aggravation)
        {
            caseTrace.addTrace(String.format("Aggravation cases not yet supported."));
            return RulesResult.createEmpty();
        }

        Optional<SoP> applicableSopOpt = condition.getProcessingRule().getApplicableSop(condition, serviceHistory, isOperational);
        if (!applicableSopOpt.isPresent())
        {
            caseTrace.addTrace("No applicable SoP.");
            return RulesResult.createEmpty();
        }
        SoP applicableSop = applicableSopOpt.get();

        ImmutableList<FactorWithSatisfaction> inferredFactors = condition.getProcessingRule().getSatisfiedFactors(condition, applicableSop, serviceHistory);

        return new RulesResult(Optional.of(condition), Optional.of(applicableSop), inferredFactors);

    }

    public static SopSupportResponseDto buildSopSupportResponseDtoFromRulesResult(RulesResult rulesResult)
    {
        Optional<SoP> applicableSopOptional = rulesResult.getApplicableSop();
        ImmutableList<FactorWithSatisfaction> inferredFactorsOptional = rulesResult.getFactorWithSatisfactions();

        if (!applicableSopOptional.isPresent())
        {
            return SopSupportResponseDto.createEmpty();
        }

        SoP applicableSop = applicableSopOptional.get();

        ApplicableInstrumentDto applicableInstrumentDto = new ApplicableInstrumentDto(applicableSop.getRegisterId(),
                StoredSop.formatInstrumentNumber(applicableSop.getInstrumentNumber()),
                applicableSop.getCitation(),
                applicableSop.getEffectiveFromDate());

        List<FactorWithInferredResultDto> factorDtos =
                inferredFactorsOptional.stream().map(factorWithSatisfaction -> DtoTransformations.fromFactorWithSatisfaction(factorWithSatisfaction)).collect(Collectors.toList());

        return new SopSupportResponseDto(applicableInstrumentDto,factorDtos);
    }





}
