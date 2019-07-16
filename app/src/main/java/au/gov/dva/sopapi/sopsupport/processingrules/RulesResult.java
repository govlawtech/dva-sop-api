package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.dtos.sopsupport.CaseTraceDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.ApplicableInstrumentDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.FactorWithInferredResultDto;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import au.gov.dva.sopapi.sopsupport.ConditionFactory;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class RulesResult {

    private Optional<Condition> condition;
    private final Optional<SoP> applicableSop;
    private final ImmutableList<FactorWithSatisfaction> factorWithSatisfactions;
    private CaseTrace caseTrace;
    private Recommendation recommendation;

    public static RulesResult createEmpty(CaseTrace caseTrace) {
        return new RulesResult(Optional.empty(),Optional.empty(),ImmutableList.of(), caseTrace, Recommendation.REJECT);
    }

    private static RulesResult applyRulesForCondition(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace)
    {
            ProcessingRule processingRule = condition.getProcessingRule();
            Optional<SoP> applicableSopOpt = processingRule.getApplicableSop(condition,serviceHistory,isOperational,caseTrace);
            if (!applicableSopOpt.isPresent())
            {
                caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, "No applicable SoP.");
                return RulesResult.createEmpty(caseTrace);
            }

            ImmutableList<FactorWithSatisfaction> inferredFactors =  processingRule.getSatisfiedFactors(condition, applicableSopOpt.get(), serviceHistory, caseTrace);
            Recommendation recommendation = processingRule.inferRecommendation(inferredFactors,serviceHistory,applicableSopOpt.get(),condition,isOperational,caseTrace);
            return new RulesResult(Optional.of(condition),applicableSopOpt,inferredFactors,caseTrace,recommendation);
    }

    private static Optional<RulesResult> checkVeteranPreconditions(SopSupportRequestDto sopSupportRequestDto, ServiceHistory serviceHistory, CaseTrace caseTrace)
    {
        if (sopSupportRequestDto.get_conditionDto().get_incidentType() == IncidentType.Aggravation)
        {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, "Computer Based Decision making does not include aggravation cases.");
            return Optional.of(RulesResult.createEmpty(caseTrace));
        }

        if (ProcessingRuleFunctions.conditionIsBeforeHireDate(sopSupportRequestDto,serviceHistory)) {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("Condition onset started on %s, before hire date of %s, therefore no SoP factors are applicable.", sopSupportRequestDto.get_conditionDto().get_incidentDateRangeDto(), serviceHistory.getHireDate()));
            return Optional.of(RulesResult.createEmpty(caseTrace));
        }

        if (ProcessingRuleFunctions.conditionIsBeforeFirstDateOfService(sopSupportRequestDto, serviceHistory)) {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("Service history shows no service before the condition start date (%s), therefore no SoP factors are applicable.", sopSupportRequestDto.get_conditionDto().get_incidentDateRangeDto().get_startDate()));
            return Optional.of(RulesResult.createEmpty(caseTrace));
        }
        return Optional.empty();
    }

    public static class ResultComparator implements Comparator<RulesResult>
    {
        ImmutableList recommendationsOrderedBestToWorst =
                ImmutableList.of(Recommendation.APPROVED,Recommendation.CHECK_RH_BOP_MET, Recommendation.CHECK_RH,Recommendation.REJECT);


        @Override
        public int compare(RulesResult o1, RulesResult o2) {
            return Integer.compare(recommendationsOrderedBestToWorst.indexOf(o1),recommendationsOrderedBestToWorst.indexOf(o2));
        }

    }

    public static ImmutableList<RulesResult> orderResultsFromMostBeneficialToLeast(ImmutableList<RulesResult> results)
    {
        return results.stream().sorted(new ResultComparator()).collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));
    }

    public static RulesResult applyRules(RuleConfigurationRepository ruleConfigurationRepository, SopSupportRequestDto sopSupportRequestDto, ImmutableSet<SoPPair> sopPairs, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        Optional<SoPPair> soPPair = ConditionFactory.getSopPairForConditionName(sopPairs,sopSupportRequestDto.get_conditionDto().get_conditionName());

        if (!soPPair.isPresent())
        {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("SoP for condition '%s' is not available, so cannot apply any processing rules.  The condition name must match exactly the condition name in the SoP.",sopSupportRequestDto.get_conditionDto().get_conditionName()));
            return RulesResult.createEmpty(caseTrace);
        }

        // now we know condition
         ServiceHistory serviceHistory =
                DtoTransformations.serviceHistoryFromDto(sopSupportRequestDto.get_serviceHistoryDto())
                .filterServiceHistoryByEvents(Arrays.asList("within specified area"));

        Optional<RulesResult> veteranPreconditions = checkVeteranPreconditions(sopSupportRequestDto,serviceHistory,caseTrace);
        if (veteranPreconditions.isPresent())
        {
            return veteranPreconditions.get();
        }

        Optional<ConditionConfiguration> conditionConfiguration = ruleConfigurationRepository.getConditionConfigurationFor(soPPair.get().getConditionName());


        ImmutableSet<ApplicableWearAndTearRuleConfiguration> wearAndTearRuleConfigurations = conditionConfiguration.get().getApplicableRuleConfigurations(soPPair.get().getConditionName(), sopSupportRequestDto.get_conditionDto().get_incidentDateRangeDto().get_startDate(),serviceHistory,caseTrace);

        // bifurcate here based on whether acute or wear and tear condition

        ImmutableSet<Condition> conditions = null;
        boolean isAcute = false;
        if (!wearAndTearRuleConfigurations.isEmpty()) {
              conditions = wearAndTearRuleConfigurations.stream()
                .map(ac -> ConditionFactory.createWearAndTearCondition(soPPair.get(),sopSupportRequestDto.get_conditionDto(),ac))
                      .filter(c -> c.isPresent())
                      .map(c -> c.get())
                      .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableSet::copyOf));
        }
        else {
            // must be acute condition
            isAcute = true;
            Optional<Condition> acuteConditionOptional = ConditionFactory.createAcuteCondition(soPPair.get(), sopSupportRequestDto.get_conditionDto());
            if (acuteConditionOptional.isPresent()) conditions = ImmutableSet.of(acuteConditionOptional.get());
        }

        if (conditions.isEmpty())
        {
                caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("No 'Computer Based Decision' rules are configured for the condition '%s'.",sopSupportRequestDto.get_conditionDto().get_conditionName()));
                return RulesResult.createEmpty(caseTrace);
        }


        // now we have all the conditions
        if (isAcute) {

            RulesResult acuteRulesResult = applyRulesForCondition(conditions.asList().get(0),serviceHistory,isOperational,caseTrace);
            return acuteRulesResult;
        }
        else {
            // wear and tear
            ImmutableList<RulesResult> wearAndTearRulesResults =
                    conditions.stream()
                            .map(c -> applyRulesForCondition(c,serviceHistory,isOperational, new SopSupportCaseTrace("tofix")))
                            .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));

            ImmutableList<RulesResult> orderedResults = orderResultsFromMostBeneficialToLeast(wearAndTearRulesResults);
            RulesResult bestResult = orderedResults.get(0);
            return bestResult;
        }



    }

    public RulesResult(Optional<Condition> condition, Optional<SoP> applicableSop, ImmutableList<FactorWithSatisfaction> factorWithSatisfactions,CaseTrace caseTrace, Recommendation recommendation)
    {
        this.condition = condition;

        this.applicableSop = applicableSop;
        this.factorWithSatisfactions = factorWithSatisfactions;
        this.caseTrace = caseTrace;
        this.recommendation = recommendation;
    }

    public Optional<SoP> getApplicableSop() {
        return applicableSop;
    }

    public ImmutableList<FactorWithSatisfaction> getFactorWithSatisfactions() {
        return factorWithSatisfactions;
    }

    public List<Factor> getSatisfiedFactors() {
        return getSatisfiedFactorsFromList(getFactorWithSatisfactions());
    }

    private static List<Factor> getSatisfiedFactorsFromList(List<FactorWithSatisfaction> factorWithSatisfactions) {
        return factorWithSatisfactions.stream()
                .filter(f -> f.isSatisfied())
                .map(f -> f.getFactor())
                .collect(toList());
    }

    public Optional<Condition> getCondition() {
        return condition;
    }

    public CaseTrace getCaseTrace() {
        return caseTrace;
    }

    public Recommendation getRecommendation() { return recommendation; }

    public boolean isEmpty() {
        return (!condition.isPresent() || !applicableSop.isPresent() || factorWithSatisfactions.isEmpty());
    }

    public SopSupportResponseDto buildSopSupportResponseDto()
    {
        Optional<SoP> applicableSopOptional = getApplicableSop();
        ImmutableList<FactorWithSatisfaction> inferredFactorsOptional = getFactorWithSatisfactions();

        ApplicableInstrumentDto applicableInstrumentDto = null;
        List<FactorWithInferredResultDto> factorDtos = ImmutableList.of();

        if (applicableSopOptional.isPresent())
        {
            SoP applicableSop = applicableSopOptional.get();

            applicableInstrumentDto = new ApplicableInstrumentDto(applicableSop.getRegisterId(),
                    StoredSop.formatInstrumentNumber(applicableSop.getInstrumentNumber()),
                    applicableSop.getCitation(),
                    applicableSop.getConditionName(),
                    applicableSop.getEffectiveFromDate(),
                    applicableSop.getStandardOfProof());

            factorDtos =
                    inferredFactorsOptional.stream().map(factorWithSatisfaction -> DtoTransformations.fromFactorWithSatisfaction(factorWithSatisfaction)).collect(Collectors.toList());
        }

        CaseTraceDto caseTraceDto = DtoTransformations.caseTraceDtoFromCaseTrace(getCaseTrace());
        return new SopSupportResponseDto(applicableInstrumentDto,factorDtos, recommendation, caseTraceDto);
    }


}
