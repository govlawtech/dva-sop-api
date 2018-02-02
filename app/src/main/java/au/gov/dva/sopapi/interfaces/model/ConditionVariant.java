package au.gov.dva.sopapi.interfaces.model;

import com.google.common.collect.ImmutableList;

public interface ConditionVariant {
    String getName();
    ImmutableList<ConditionVariantFactor> getVariantFactors();
}
