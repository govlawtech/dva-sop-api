package au.gov.dva.sopapi.sopref.data.sops;

import au.gov.dva.sopapi.interfaces.model.DefinedTerm;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.SubFactor;
import com.google.common.collect.ImmutableSet;

import java.util.List;

public class StoredFactor implements Factor {

    private final String paragraph;
    private final String text;
    private final ImmutableSet<DefinedTerm> definedTerms;

    public StoredFactor(String paragraph, String text, ImmutableSet<DefinedTerm> definedTerms) {
        this.paragraph = paragraph;
        this.text = text;
        this.definedTerms = definedTerms;
    }

    @Override
    public String getParagraph() {
        return paragraph;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public List<SubFactor> getSubFactors() {
        return null;
    }

    @Override
    public ImmutableSet<DefinedTerm> getDefinedTerms() {
        return definedTerms;
    }

}
