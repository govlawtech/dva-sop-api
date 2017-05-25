package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.function.Predicate;

public class LumbarSpondylosisRule extends GenericProcessingRule implements ProcessingRule, AccumulationRule {

    public LumbarSpondylosisRule(RuleConfigurationRepository ruleConfigurationRepository) {
        super(ruleConfigurationRepository);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        ImmutableList<Factor> applicableFactors =  condition.getApplicableFactors(applicableSop);

        caseTrace.addLoggingTrace(String.format("Determining whether condition started within 25 year of the last day of MRCA service..."));
        if (!ProcessingRuleFunctions.conditionStartedWithinXYearsOfLastDayOfMRCAService(condition,serviceHistory,25, caseTrace)) {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, String.format("Lumbar spondylosis did not start within 25 years of the last day of MRCA service, therefore no factors satisfied."));
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, ImmutableSet.of());
        }
        return super.getSatisfiedFactors(condition,applicableSop,serviceHistory, caseTrace);
    }

    @Override
    public Long getAccumulation() {
        return null;
    }

    @Override
    public String getAccumulationUnit() {
        return "kg";
    }


}