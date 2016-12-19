package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.*;
import com.google.common.collect.ImmutableSet;

import java.util.function.Predicate;

public interface ProcessingRule {

    ImmutableSet<String> appliesToInstrumentIds();
    SoP getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational);
    ImmutableSet<Factor> getApplicableFactors(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational);
    ImmutableSet<Factor> getSatisfiedFactors(Condition condition, ServiceHistory serviceHistory);
}


