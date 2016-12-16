package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
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
