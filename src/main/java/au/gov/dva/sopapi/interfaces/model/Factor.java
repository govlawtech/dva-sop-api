package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableSet;

public interface Factor {
    String getParagraph();
    String getText();
    ImmutableSet<DefinedTerm> getDefinedTerms();
}

