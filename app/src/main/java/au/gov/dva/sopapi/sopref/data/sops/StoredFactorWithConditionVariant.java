package au.gov.dva.sopapi.sopref.data.sops;

import au.gov.dva.sopapi.interfaces.model.ConditionVariant;
import au.gov.dva.sopapi.interfaces.model.DefinedTerm;
import au.gov.dva.sopapi.interfaces.model.Factor;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;

public class StoredFactorWithConditionVariant implements Factor {

    private final Factor toDecorate;
    private final ConditionVariant conditionVariant;

    public StoredFactorWithConditionVariant(Factor toDecorate, ConditionVariant conditionVariant)
    {

        this.toDecorate = toDecorate;
        this.conditionVariant = conditionVariant;
    }

    @Override
    public String getParagraph() {
        return toDecorate.getParagraph();
    }

    @Override
    public String getText() {
        return toDecorate.getText();
    }

    @Override
    public ImmutableSet<DefinedTerm> getDefinedTerms() {
        return toDecorate.getDefinedTerms();
    }

    @Override
    public Optional<ConditionVariant> getConditionVariant() {
        return Optional.of(conditionVariant);
    }
}
