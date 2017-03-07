package au.gov.dva.sopapi.sopref.data.sops;

import au.gov.dva.sopapi.interfaces.model.DefinedTerm;
import au.gov.dva.sopapi.interfaces.model.Factor;
import com.google.common.collect.ImmutableSet;

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
    public ImmutableSet<DefinedTerm> getDefinedTerms() {
        return definedTerms;
    }

}
