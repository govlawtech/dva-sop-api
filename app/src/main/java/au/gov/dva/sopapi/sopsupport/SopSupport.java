package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.sopsupport.CaseTraceDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.ApplicableInstrumentDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.FactorWithInferredResultDto;
import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import au.gov.dva.sopapi.sopsupport.processingrules.RuleConfigRepositoryUtils;
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
            caseTrace.addLoggingTrace(String.format("SoP for condition %s is not implemented, so cannot apply any processing rules.",sopSupportRequestDto.get_conditionDto().get_conditionName()));
            return RulesResult.createEmpty(caseTrace);
        }
        Condition condition = conditionOptional.get();
        ServiceHistory serviceHistory = DtoTransformations.serviceHistoryFromDto(sopSupportRequestDto.get_serviceHistoryDto());

        if (ProcessingRuleFunctions.conditionIsBeforeService(condition, serviceHistory)) {
            caseTrace.addLoggingTrace(String.format("Condition onset started on %s, before hire date of %s, therefore no SoP factors are applicable.", condition.getStartDate(), serviceHistory.getHireDate()));
            return  RulesResult.createEmpty(caseTrace);
        }

        if (sopSupportRequestDto.get_conditionDto().get_incidentType() == IncidentType.Aggravation)
        {
            caseTrace.addLoggingTrace(String.format("Aggravation cases not yet supported."));
            return RulesResult.createEmpty(caseTrace);
        }

        Optional<SoP> applicableSopOpt = condition.getProcessingRule().getApplicableSop(condition, serviceHistory, isOperational,caseTrace);
        if (!applicableSopOpt.isPresent())
        {
            caseTrace.addLoggingTrace("No applicable SoP.");
            return RulesResult.createEmpty(caseTrace);
        }
        SoP applicableSop = applicableSopOpt.get();
        caseTrace.addLoggingTrace("Applicable SoP is " + applicableSop.getCitation());
        ImmutableList<FactorWithSatisfaction> inferredFactors = condition.getProcessingRule().getSatisfiedFactors(condition, applicableSop, serviceHistory,caseTrace);

        // get rh factors and attach to Case Trace
        Optional<Rank> relevantRank = ProcessingRuleFunctions.getRankProximateToDate(serviceHistory.getServices(),condition.getStartDate(),caseTrace);
        Optional<Service> serviceDuringWhichConditionStarts =  ProcessingRuleFunctions.identifyServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(),condition.getStartDate(),caseTrace);
        if (relevantRank.isPresent() && serviceDuringWhichConditionStarts.isPresent())
        {
            ImmutableList<Factor> rhFactors = condition.getApplicableFactors(condition.getSopPair().getRhSop());
            Optional<RHRuleConfigurationItem> RHRuleConfigItemOpt = RuleConfigRepositoryUtils.getRelevantRHConfiguration(condition.getSopPair().getConditionName(),
                    relevantRank.get(),
                    serviceDuringWhichConditionStarts.get().getBranch(),
                    ruleConfigurationRepository);
            if (RHRuleConfigItemOpt.isPresent()){
                ImmutableSet<String> rhFactorParagraphs = RHRuleConfigItemOpt.get().getFactorReferences();
                List<Factor> applicableRhFactors = rhFactors.stream().filter(f -> rhFactorParagraphs.contains(f.getParagraph())).collect(Collectors.toList());
                caseTrace.setRhFactors(ImmutableList.copyOf(applicableRhFactors));
            }
        }

        return new RulesResult(Optional.of(condition), Optional.of(applicableSop), inferredFactors, caseTrace);

    }

    public static SopSupportResponseDto buildSopSupportResponseDtoFromRulesResult(RulesResult rulesResult)
    {
        Optional<SoP> applicableSopOptional = rulesResult.getApplicableSop();
        ImmutableList<FactorWithSatisfaction> inferredFactorsOptional = rulesResult.getFactorWithSatisfactions();

        ApplicableInstrumentDto applicableInstrumentDto = null;
        List<FactorWithInferredResultDto> factorDtos = ImmutableList.of();

        if (applicableSopOptional.isPresent())
        {
            SoP applicableSop = applicableSopOptional.get();

            applicableInstrumentDto = new ApplicableInstrumentDto(applicableSop.getRegisterId(),
                StoredSop.formatInstrumentNumber(applicableSop.getInstrumentNumber()),
                applicableSop.getCitation(),
                applicableSop.getEffectiveFromDate(),
                    applicableSop.getStandardOfProof());

            factorDtos =
                inferredFactorsOptional.stream().map(factorWithSatisfaction -> DtoTransformations.fromFactorWithSatisfaction(factorWithSatisfaction)).collect(Collectors.toList());
        }

        CaseTraceDto caseTraceDto = DtoTransformations.caseTraceDtoFromCaseTrace(rulesResult.getCaseTrace());
        return new SopSupportResponseDto(applicableInstrumentDto,factorDtos,caseTraceDto);
    }





}
