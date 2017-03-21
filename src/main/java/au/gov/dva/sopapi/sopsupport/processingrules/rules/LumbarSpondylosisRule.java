package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.function.Predicate;

public class LumbarSpondylosisRule extends ProcessingRuleBase implements ProcessingRule, AccumulationRule {

    private RuleConfigurationRepository ruleConfigurationRepository;
    public LumbarSpondylosisRule(RuleConfigurationRepository ruleConfigurationRepository) {
        this.ruleConfigurationRepository = ruleConfigurationRepository;
    }



    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational) {
        return super.getApplicableSop(ruleConfigurationRepository,condition,serviceHistory,isOperational);
    }


    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory) {
        ImmutableList<Factor> applicableFactors =  condition.getApplicableFactors(applicableSop);

        if (!ProcessingRuleFunctions.conditionStartedWithinXYearsOfLastDayOfMRCAService(condition,serviceHistory,25))
            return ProcessingRuleFunctions.withSatisfiedFactors(applicableFactors, ImmutableSet.of());

        return super.getSatisfiedFactors(ruleConfigurationRepository,condition,applicableSop,serviceHistory);
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