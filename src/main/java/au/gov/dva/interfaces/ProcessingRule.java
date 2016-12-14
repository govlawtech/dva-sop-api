package au.gov.dva.interfaces;

import au.gov.dva.interfaces.model.Condition;
import au.gov.dva.interfaces.model.Factor;
import au.gov.dva.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableSet;

public interface ProcessingRule {
    ImmutableSet<Factor> getApplicableFactors(Condition condition, ServiceHistory serviceHistory);
    ImmutableSet<Factor> getSatisfiedFactors(Condition condition, ServiceHistory serviceHistory);
}


