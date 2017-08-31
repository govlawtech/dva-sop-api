package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.dtos.sopsupport.CaseTraceDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.ApplicableInstrumentDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.FactorWithInferredResultDto;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import au.gov.dva.sopapi.sopsupport.ConditionFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
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

    public static RulesResult applyRules(RuleConfigurationRepository ruleConfigurationRepository, SopSupportRequestDto sopSupportRequestDto, ImmutableSet<SoPPair> sopPairs, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        // TODO: compare register ID in the sop pairs and the rule config.  If different, suggests there has been an update to the SoP.  Hence, the rules need to be updated
        if (sopSupportRequestDto.get_conditionDto().get_incidentType() == IncidentType.Aggravation)
        {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, "Aggravation cases not covered.");
            return RulesResult .createEmpty(caseTrace);
        }

        Optional<SoPPair> soPPair = ConditionFactory.getSopPairForConditionName(sopPairs,sopSupportRequestDto.get_conditionDto().get_conditionName());

        if (!soPPair.isPresent())
        {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("SoP for condition '%s' is not available, so cannot apply any processing rules.  The condition name must match exactly the condition name in the SoP.",sopSupportRequestDto.get_conditionDto().get_conditionName()));
            return RulesResult.createEmpty(caseTrace);
        }

        Optional<Condition> conditionOptional = ConditionFactory.create(soPPair.get(), sopSupportRequestDto.get_conditionDto(),ruleConfigurationRepository);

        if (!conditionOptional.isPresent())
        {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format(String.format("No processing rule is configured for condition: '%s'.  The condition name must match exactly the name from the SoP.", sopSupportRequestDto.get_conditionDto().get_conditionName())));
            return  RulesResult.createEmpty(caseTrace);
        }

        Condition condition = conditionOptional.get();

        ServiceHistory serviceHistory = DtoTransformations.serviceHistoryFromDto(sopSupportRequestDto.get_serviceHistoryDto());
        serviceHistory = serviceHistory.filterServiceHistoryByEvents(Arrays.asList("within specified area"));

        if (ProcessingRuleFunctions.conditionIsBeforeHireDate(condition, serviceHistory)) {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("Condition onset started on %s, before hire date of %s, therefore no SoP factors are applicable.", condition.getStartDate(), serviceHistory.getHireDate()));
            return  RulesResult.createEmpty(caseTrace);
        }

        if (ProcessingRuleFunctions.conditionIsBeforeFirstDateOfService(condition, serviceHistory)) {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("Service history shows no service before the condition start date (%s), therefore no SoP factors are applicable.", condition.getStartDate()));
            return  RulesResult.createEmpty(caseTrace);
        }


        Optional<SoP> applicableSopOpt = condition.getProcessingRule().getApplicableSop(condition, serviceHistory, isOperational,caseTrace);
        if (!applicableSopOpt.isPresent())
        {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, "No applicable SoP.");
            return RulesResult.createEmpty(caseTrace);
        }
        SoP applicableSop = applicableSopOpt.get();

        ImmutableList<FactorWithSatisfaction> inferredFactors = condition.getProcessingRule().getSatisfiedFactors(condition, applicableSop, serviceHistory,caseTrace);

        condition.getProcessingRule().attachConfiguredFactorsToCaseTrace(condition,serviceHistory, caseTrace);

        Recommendation recommendation = condition.getProcessingRule().inferRecommendation(inferredFactors,serviceHistory,applicableSop,condition,isOperational, caseTrace);

        return new RulesResult(Optional.of(condition), Optional.of(applicableSop), inferredFactors, caseTrace, recommendation);

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
                    applicableSop.getEffectiveFromDate(),
                    applicableSop.getStandardOfProof());

            factorDtos =
                    inferredFactorsOptional.stream().map(factorWithSatisfaction -> DtoTransformations.fromFactorWithSatisfaction(factorWithSatisfaction)).collect(Collectors.toList());
        }

        CaseTraceDto caseTraceDto = DtoTransformations.caseTraceDtoFromCaseTrace(getCaseTrace());
        return new SopSupportResponseDto(applicableInstrumentDto,factorDtos, recommendation, caseTraceDto);
    }


}
