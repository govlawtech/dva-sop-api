package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.FactorWithSatisfaction;
import au.gov.dva.sopapi.interfaces.model.SoP;
import com.google.common.collect.ImmutableList;

import java.util.Optional;

public class RulesResult {

    private Optional<Condition> condition;
    private final Optional<SoP> applicableSop;
    private final ImmutableList<FactorWithSatisfaction> factorWithSatisfactions;
    private CaseTrace caseTrace;

    public static RulesResult createEmpty(CaseTrace caseTrace) {
        return new RulesResult(Optional.empty(),Optional.empty(),ImmutableList.of(), caseTrace);
    }

    public RulesResult(Optional<Condition> condition, Optional<SoP> applicableSop, ImmutableList<FactorWithSatisfaction> factorWithSatisfactions,CaseTrace caseTrace)
    {
        this.condition = condition;

        this.applicableSop = applicableSop;
        this.factorWithSatisfactions = factorWithSatisfactions;
        this.caseTrace = caseTrace;
    }

    public Optional<SoP> getApplicableSop() {
        return applicableSop;
    }

    public ImmutableList<FactorWithSatisfaction> getFactorWithSatisfactions() {
        return factorWithSatisfactions;
    }

    public Optional<Condition> getCondition() {
        return condition;
    }

    public CaseTrace getCaseTrace() {
        return caseTrace;
    }

    public boolean isEmpty() {
        return (!condition.isPresent() || !applicableSop.isPresent() || factorWithSatisfactions.isEmpty());
    }
}
