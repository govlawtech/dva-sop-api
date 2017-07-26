package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import com.google.common.collect.ImmutableList;
import scala.Int;

import java.util.Optional;
import java.util.function.Predicate;

public interface ProcessingRule {

    ApplicableSopResult getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace);
    ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace);
    void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace);
}

