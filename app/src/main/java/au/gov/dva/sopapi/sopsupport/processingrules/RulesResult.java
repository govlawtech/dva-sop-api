package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.*;
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
import au.gov.dva.sopapi.veaops.interfaces.VeaOperationalServiceRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

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

    public static RulesResult createEmpty(Condition condition, CaseTrace caseTrace)
    {
        return new RulesResult(Optional.of(condition),Optional.empty(),ImmutableList.of(), caseTrace, Recommendation.REJECT);
    }

    private static RulesResult applyRulesForCondition(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, VeaOperationalServiceRepository veaOperationalServiceRepository, ImmutableSet<ServiceDetermination> serviceDeterminations, CaseTrace caseTrace)
    {
            ProcessingRule processingRule = condition.getProcessingRule();
            Optional<SoP> applicableSopOpt = processingRule.getApplicableSop(condition,serviceHistory,isOperational,caseTrace);
            if (!applicableSopOpt.isPresent())
            {
                caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, "No applicable SoP.");
                return RulesResult.createEmpty(condition, caseTrace);
            }

            ImmutableList<FactorWithSatisfaction> inferredFactors =  processingRule.getSatisfiedFactors(condition, applicableSopOpt.get(), serviceHistory, caseTrace);
            Recommendation recommendation = processingRule.inferRecommendation(inferredFactors,serviceHistory,applicableSopOpt.get(),condition,isOperational,caseTrace);
            ProcessingRuleFunctions.inferRelevantOperations(serviceHistory,condition,veaOperationalServiceRepository,serviceDeterminations,isOperational,caseTrace);

            return new RulesResult(Optional.of(condition),applicableSopOpt,inferredFactors,caseTrace,recommendation);
    }

    private static Optional<RulesResult> checkServiceHistoryPreconditions(ServiceHistory serviceHistory, CaseTrace caseTrace)
    {
        ImmutableList<Deployment> deploymentsWhereEndDateIsBeforeStartDate =
                serviceHistory.getCftsDeployments().stream().filter(d -> !d.isValid()).collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));
        if (!deploymentsWhereEndDateIsBeforeStartDate.isEmpty())
        {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING,"The service history is not internally consistent: at least one end date is before the corresponding start date.");
            return Optional.of(RulesResult.createEmpty(caseTrace));
        }
        return Optional.empty();
    }

    private static Optional<RulesResult> checkVeteranPreconditions(SopSupportRequestDto sopSupportRequestDto, ServiceHistory serviceHistory, CaseTrace caseTrace)
    {
        if (sopSupportRequestDto.get_conditionDto().get_incidentType() == IncidentType.Aggravation)
        {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, "Computer Based Decision making does not include aggravation cases.");
            return Optional.of(RulesResult.createEmpty(caseTrace));
        }

        if (ProcessingRuleFunctions.conditionIsBeforeHireDate(sopSupportRequestDto,serviceHistory)) {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("Condition onset started on %s, before hire date of %s, therefore no SoP factors are applicable.", sopSupportRequestDto.get_conditionDto().get_incidentDateRangeDto().get_startDate(), serviceHistory.getHireDate()));
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

    private static RulesResult applyRulesForAcuteCondition(SopSupportRequestDto sopSupportRequestDto, ServiceHistory serviceHistory, SoPPair soPPair, Predicate<Deployment> isOperational, VeaOperationalServiceRepository veaOperationalServiceRepository, ImmutableSet<ServiceDetermination> serviceDeterminations, CaseTrace caseTrace)
    {
        Optional<Condition> acuteConditionOptional = ConditionFactory.createAcuteCondition(soPPair, sopSupportRequestDto.get_conditionDto());
        assert acuteConditionOptional.isPresent() : "check config before creating";
        RulesResult rulesResult = applyRulesForCondition(acuteConditionOptional.get(),serviceHistory,isOperational, veaOperationalServiceRepository, serviceDeterminations, caseTrace);
        return rulesResult;
    }

    private static RulesResult applyRulesForWearAndTearCondition(ConditionConfiguration conditionConfiguration, SoPPair soPPair, SopSupportRequestDto sopSupportRequestDto, ServiceHistory serviceHistory, Predicate<Deployment> isOperational , VeaOperationalServiceRepository veaOperationalServiceRepository, ImmutableSet<ServiceDetermination> serviceDeterminations)
    {
        CaseTrace localCt = new SopSupportCaseTrace();
        ImmutableSet<ApplicableWearAndTearRuleConfiguration> wearAndTearRuleConfigurations = conditionConfiguration.getApplicableRuleConfigurations(soPPair.getConditionName(), sopSupportRequestDto.get_conditionDto().get_incidentDateRangeDto().get_startDate(),serviceHistory,localCt);

        if (wearAndTearRuleConfigurations.isEmpty())
        {
            localCt.addReasoningFor(ReasoningFor.ABORT_PROCESSING,"None of the configured processing rules match the service history.");
            return RulesResult.createEmpty(localCt);
        }

        ImmutableSet<Condition> conditions = wearAndTearRuleConfigurations.stream()
                .map(ac -> ConditionFactory.createWearAndTearCondition(soPPair,sopSupportRequestDto.get_conditionDto(),ac))
                .filter(c -> c.isPresent())
                .map(c -> c.get())
                .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableSet::copyOf));

        ImmutableList<RulesResult> wearAndTearRulesResults =
                conditions.stream()
                        .map(c -> applyRulesForCondition(c,serviceHistory,isOperational, veaOperationalServiceRepository, serviceDeterminations,  new SopSupportCaseTrace(c.getSopPair().getConditionName())))
                        .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));
        ImmutableList<RulesResult> orderedResults = orderResultsFromMostBeneficialToLeast(wearAndTearRulesResults);
        RulesResult bestResult = orderedResults.get(0);

        if (bestResult.condition.isPresent() && bestResult.condition.get().getProcessingRule() instanceof WearAndTearProcessingRule)
        {
            WearAndTearProcessingRule appliedRule = (WearAndTearProcessingRule)bestResult.condition.get().getProcessingRule();
            ApplicableWearAndTearRuleConfiguration appliedConfig = appliedRule.getApplicableWearAndTearRuleConfiguration();
            ServiceBranch serviceBranch = appliedConfig.getRHRuleConfigurationItem().getServiceBranch();
            if (wearAndTearRulesResults.size() > 1) {
                bestResult.caseTrace.addReasoningFor(ReasoningFor.STANDARD_OF_PROOF, String.format("The Computer Based Decision rules for the service branch '%s' applied because this yielded the most beneficial result.", serviceBranch.toString()));
                bestResult.caseTrace.addReasoningFor(ReasoningFor.MEETING_FACTORS, String.format("The Computer Based Decision rules for the service branch '%s' applied because this yielded the most beneficial result.", serviceBranch.toString()));
            }
        }


        return wearAndTearRulesResults.get(0);
    }

    private enum ConditionType {
        Acute,
        WearAndTear,
        NotConfigured
    }

    private static ConditionType getConditionType(String conditionName, RuleConfigurationRepository repository)
    {
        if (ConditionFactory.getAcuteConditions().contains(conditionName)) return ConditionType.Acute;
        if (repository.getConditionConfigurationFor(conditionName).isPresent()) return ConditionType.WearAndTear;
        return ConditionType.NotConfigured;
    }


    public static RulesResult applyRules(RuleConfigurationRepository ruleConfigurationRepository, SopSupportRequestDto sopSupportRequestDto, ImmutableSet<SoPPair> sopPairs, IsOperationalPredicateFactory isOperationalPredicateFactory, VeaOperationalServiceRepository veaOperationalServiceRepository, ImmutableSet<ServiceDetermination> serviceDeterminations, CaseTrace caseTrace) {

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

        Optional<RulesResult> serviceHistoryConsistency = checkServiceHistoryPreconditions(serviceHistory,caseTrace);
        if (serviceHistoryConsistency.isPresent())
        {
            return serviceHistoryConsistency.get();
        }


        ConditionType conditionType = getConditionType(soPPair.get().getConditionName(), ruleConfigurationRepository);
        if (conditionType == ConditionType.NotConfigured)
        {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("No 'Computer Based Decision' rules are configured for the condition '%s'.",sopSupportRequestDto.get_conditionDto().get_conditionName()));
            return RulesResult.createEmpty(caseTrace);
        }

        Predicate<Deployment> nonDateValidatingIsOperationalPredicate = isOperationalPredicateFactory.createMrcaOrVeaPredicate(sopSupportRequestDto.get_conditionDto(),false,caseTrace);
        if (conditionType == ConditionType.Acute)
        {
            RulesResult rulesResult = applyRulesForAcuteCondition(sopSupportRequestDto, serviceHistory, soPPair.get(), nonDateValidatingIsOperationalPredicate , veaOperationalServiceRepository, serviceDeterminations,  caseTrace);
            return rulesResult;
        }
        else // (conditionType == ConditionType.WearAndTear)
        {
            assert conditionType == ConditionType.WearAndTear;
            Optional<ConditionConfiguration> conditionConfiguration = ruleConfigurationRepository.getConditionConfigurationFor(soPPair.get().getConditionName());
            assert conditionConfiguration.isPresent() : "Check whether wear and tear configuration present before getting.";
            RulesResult rulesResult = applyRulesForWearAndTearCondition(conditionConfiguration.get(),soPPair.get(), sopSupportRequestDto,serviceHistory,nonDateValidatingIsOperationalPredicate, veaOperationalServiceRepository, serviceDeterminations);
            return rulesResult;
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
