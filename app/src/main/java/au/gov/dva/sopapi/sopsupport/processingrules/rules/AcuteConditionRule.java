package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.exceptions.ProcessingRuleRuntimeException;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class AcuteConditionRule implements AcuteProcessingRule {

    private final ImmutableSet<String> _satisfiedRHFactorParas;
    private final ImmutableSet<String> _satisfiedBoPFactorPara;
    private final ImmutableSet<String> _registerIds;
    private Function<Condition, Interval> _providerForIntervalToCheckForOperationalService;


    public AcuteConditionRule(String rhRegisterId, ImmutableSet<String> rhFactorParas, String bopRegisterId, ImmutableSet<String> bopFactorParas, Function<Condition, Interval> providerForIntervalToCheckForOperationalService) {
        _providerForIntervalToCheckForOperationalService = providerForIntervalToCheckForOperationalService;
        _registerIds = ImmutableSet.of(rhRegisterId, bopRegisterId);
        _satisfiedRHFactorParas = rhFactorParas;
        _satisfiedBoPFactorPara = bopFactorParas;
    }

    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        Optional<Service> cftsOnOnset = serviceHistory.findCftsOnDate(condition.getStartDate());
        if (!cftsOnOnset.isPresent()) {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("The service history does not show any continuous full time service on the condition onset date (%s).  This is required for the condition '%s'.", DateTimeFormatter.ISO_LOCAL_DATE.format(condition.getStartDate()), condition.getSopPair().getConditionName()));
            return Optional.empty();
        }

        Interval intervalToCheckForOperationalService = _providerForIntervalToCheckForOperationalService.apply(condition);
        long numberOfDaysOfFullTimeOperationalServiceDaysInInterval = serviceHistory.getNumberOfDaysOfFullTimeOperationalService(intervalToCheckForOperationalService.getStart(), intervalToCheckForOperationalService.getEnd(), isOperational);
        if (numberOfDaysOfFullTimeOperationalServiceDaysInInterval >= Integer.MAX_VALUE)
            throw new ProcessingRuleRuntimeException("Number of operational days above max value.");


        caseTrace.setActualOperationalDays((int) numberOfDaysOfFullTimeOperationalServiceDaysInInterval);


        boolean hasOperationalServiceInWindow = numberOfDaysOfFullTimeOperationalServiceDaysInInterval > 0;

        Function<ServiceHistory, Optional<SoP>> determineApplicableSop = sh -> {
            Optional<Deployment> deploymentDuringWhichConditionOccurred = serviceHistory.findDeploymentOnDate(condition.getStartDate());

            if (deploymentDuringWhichConditionOccurred.isPresent()) {
                if (isOperational.test(deploymentDuringWhichConditionOccurred.get())) {
                    caseTrace.addReasoningFor(ReasoningFor.STANDARD_OF_PROOF, String.format("The standard of proof is reasonable hypothesis because the service history shows a warlike or non-warlike operation on the condition start date (%s): %s", DateTimeFormatter.ISO_LOCAL_DATE.format(condition.getStartDate()), deploymentDuringWhichConditionOccurred.get().getOperationName()));

                    return Optional.of(condition.getSopPair().getRhSop());
                }
            } else if (hasOperationalServiceInWindow) {
                caseTrace.addReasoningFor(ReasoningFor.STANDARD_OF_PROOF, "The standard of proof is reasonable hypothesis because the service history shows warlike or non-warlike service proximate to the condition onset.");
                return Optional.of(condition.getSopPair().getRhSop());
            }

            caseTrace.addReasoningFor(ReasoningFor.STANDARD_OF_PROOF, "The standard of proof is balance of probabilities because the service history does not show any warlike or non-warlike service on, or proximate to, the condition onset.");

            return Optional.of(condition.getSopPair().getBopSop());
        };

        Optional<SoP> applicableSop = determineApplicableSop.apply(serviceHistory);
        if (applicableSop.isPresent() && !_registerIds.contains(applicableSop.get().getRegisterId())) {

            String configuredRegisterIds = String.join(", ", _registerIds);
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("The Register ID on the Federal Register of Legislative Instruments for the applicable SoP is %s but the rule is configured for Register IDs %s.  Most likely the SoPs have been updated but the processing rule has not.", applicableSop.get().getRegisterId(), configuredRegisterIds));
            return Optional.empty();
        } else {

            caseTrace.setConditionName(condition.getSopPair().getConditionName());
            caseTrace.setRequiredOperationalDaysForRh(1);
            caseTrace.setApplicableStandardOfProof(applicableSop.get().getStandardOfProof());
            caseTrace.setRequiredCftsDays(1);
            caseTrace.setRequiredCftsDaysForBop(1);
            caseTrace.setRequiredCftsDaysForRh(1);

            // applicable factors
            ImmutableList<Factor> applicableRhFactors =  ProcessingRuleBase.getApplicableFactors(condition.getSopPair().getRhSop(), _satisfiedRHFactorParas);
            ImmutableList<Factor> applicableBoPFactors = ProcessingRuleBase.getApplicableFactors(condition.getSopPair().getBopSop(),_satisfiedBoPFactorPara);
            caseTrace.setRhFactors(applicableRhFactors);
            caseTrace.setBopFactors(applicableBoPFactors);

            return applicableSop;
        }
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory,  CaseTrace caseTrace) {

        ImmutableList<Factor> applicableFactors = condition.getApplicableFactors(applicableSop);

        caseTrace.setActualCftsDays((int) serviceHistory.getNumberOfDaysCftsInIntervalInclusive(serviceHistory.getStartofService().get(), condition.getStartDate()));

        caseTrace.addReasoningFor(ReasoningFor.MEETING_FACTORS, String.format("The injury '%s' is acute and occurred during continuous full-time service.  Therefore, it is reasonably likely the factor is met.",condition.getSopPair().getConditionName()));

        switch (applicableSop.getStandardOfProof()) {
            case ReasonableHypothesis:
                return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, _satisfiedRHFactorParas);
            case BalanceOfProbabilities:
                return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, _satisfiedBoPFactorPara);
            default:
                throw new ProcessingRuleRuntimeException("Unknown standard of proof: " + applicableSop.getStandardOfProof());
        }
    }


    @Override
    public Recommendation inferRecommendation(ImmutableList<FactorWithSatisfaction> factors, ServiceHistory serviceHistory, SoP applicableSop, Condition condition, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        boolean satisfied = factors.stream().anyMatch(f -> f.isSatisfied());

        if (satisfied) {
            return Recommendation.APPROVED;
        } else {
            return Recommendation.REJECT;
        }

    }


}
