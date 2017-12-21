package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.FactorWithSatisfaction;

public class SatisfiedFactorWithApplicablePart implements FactorWithSatisfaction {

    private final FactorWithSatisfaction toDecorate;
    private final String applicablePart;

    public SatisfiedFactorWithApplicablePart(FactorWithSatisfaction toDecorate, String applicablePart)
    {

        this.toDecorate = toDecorate;
        this.applicablePart = applicablePart;
    }

    @Override
    public Factor getFactor() {
        return toDecorate.getFactor();
    }

    @Override
    public Boolean isSatisfied() {
        return toDecorate.isSatisfied();
    }

    @Override
    public String getApplicablePart() {
        return applicablePart;
    }
}
