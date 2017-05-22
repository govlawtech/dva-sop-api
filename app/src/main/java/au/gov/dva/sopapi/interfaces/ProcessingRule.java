package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.*;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

public interface ProcessingRule {

    Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace);
    ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace);
    void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace);

}

