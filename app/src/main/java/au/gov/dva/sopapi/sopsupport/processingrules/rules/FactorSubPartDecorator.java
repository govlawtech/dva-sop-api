package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.ConditionConfiguration;
import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.parsing.traits.SubParasHandler;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class FactorSubPartDecorator implements ProcessingRule {

    private final ProcessingRule toDecorate;
    private final ConditionConfiguration conditionConfiguration;
    private final SubParasHandler subParasHandler;

    public FactorSubPartDecorator(ProcessingRule toDecorate, ConditionConfiguration conditionConfiguration, SubParasHandler subParasHandler)
    {

        this.toDecorate = toDecorate;
        this.conditionConfiguration = conditionConfiguration;
        this.subParasHandler = subParasHandler;
    }


    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        return toDecorate.getApplicableSop(condition,serviceHistory,isOperational,caseTrace);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        return null;
    }

    @Override
    public void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        toDecorate.attachConfiguredFactorsToCaseTrace(condition,serviceHistory,caseTrace);
    }

    @Override
    public Recommendation inferRecommendation(ImmutableList<FactorWithSatisfaction> factors, ServiceHistory serviceHistory, SoP applicableSop, Condition condition, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        return toDecorate.inferRecommendation(factors,serviceHistory,applicableSop,condition,isOperational,caseTrace);
    }
}
