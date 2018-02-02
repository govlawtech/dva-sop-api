package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Factor {
    String getParagraph();
    String getText();
    ImmutableSet<DefinedTerm> getDefinedTerms();

    default Optional<ConditionVariant> getConditionVariant() {
        return Optional.empty();
    }


}

