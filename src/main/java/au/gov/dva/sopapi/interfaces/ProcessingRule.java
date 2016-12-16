package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableSet;

public interface ProcessingRule {
    ImmutableSet<Factor> getApplicableFactors(Condition condition, ServiceHistory serviceHistory);
    ImmutableSet<Factor> getSatisfiedFactors(Condition condition, ServiceHistory serviceHistory);
}


