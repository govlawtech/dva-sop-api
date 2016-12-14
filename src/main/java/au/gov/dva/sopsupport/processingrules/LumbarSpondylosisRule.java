package au.gov.dva.sopsupport.processingrules;

import au.gov.dva.interfaces.ProcessingRule;
import au.gov.dva.interfaces.model.Condition;
import au.gov.dva.interfaces.model.Factor;
import au.gov.dva.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableSet;

public class LumbarSpondylosisRule implements ProcessingRule {
    @Override
    public ImmutableSet<Factor> getApplicableFactors(Condition condition, ServiceHistory serviceHistory) {

        // call generic rule based on one day of continuous full time service
        return null;
    }

    @Override
    public ImmutableSet<Factor> getSatisfiedFactors(Condition condition, ServiceHistory serviceHistory) {
        return null;
    }


}
