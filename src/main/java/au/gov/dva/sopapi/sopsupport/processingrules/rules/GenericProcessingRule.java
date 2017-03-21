package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.*;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class GenericProcessingRule extends ProcessingRuleBase implements ProcessingRule {

    private RuleConfigurationRepository ruleConfigurationRepository;

    public GenericProcessingRule(RuleConfigurationRepository ruleConfigurationRepository)
    {
        this.ruleConfigurationRepository = ruleConfigurationRepository;
    }

    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational) {
        return super.getApplicableSop(ruleConfigurationRepository,condition,serviceHistory,isOperational);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory) {
         return super.getSatisfiedFactors(ruleConfigurationRepository,condition,applicableSop,serviceHistory);
    }
}
