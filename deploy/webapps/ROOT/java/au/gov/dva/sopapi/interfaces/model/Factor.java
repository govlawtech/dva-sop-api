package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableSet;

import java.util.List;

public interface Factor {
    String getParagraph();
    String getText();
    List<SubFactor> getSubFactors();
    ImmutableSet<DefinedTerm> getDefinedTerms();
}

