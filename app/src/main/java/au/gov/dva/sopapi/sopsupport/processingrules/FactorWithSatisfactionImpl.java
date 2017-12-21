package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.FactorWithSatisfaction;

public class FactorWithSatisfactionImpl implements FactorWithSatisfaction {

    private final Factor factor;
    private final Boolean isSatisfied;

    public FactorWithSatisfactionImpl(Factor factor, Boolean isSatisfied)
    {
        this.factor = factor;
        this.isSatisfied = isSatisfied;
    }

    @Override
    public Factor getFactor() {
        return factor;
    }

    @Override
    public Boolean isSatisfied() {
        return isSatisfied;
    }


}
