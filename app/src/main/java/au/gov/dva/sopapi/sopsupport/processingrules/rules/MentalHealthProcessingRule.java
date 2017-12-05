package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.model.*;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public class MentalHealthProcessingRule implements ProcessingRule {
    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        return null;
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        return null;
    }

    @Override
    public void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace) {

    }

    @Override
    public Recommendation inferRecommendation(ImmutableList<FactorWithSatisfaction> factors, ServiceHistory serviceHistory, SoP applicableSop, Condition condition, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        return null;
    }
}
