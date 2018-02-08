package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.interfaces.JsonSerializable;
import com.google.common.collect.ImmutableList;

public interface ConditionVariant {
    String getName();
    ImmutableList<ConditionVariantFactor> getVariantFactors();
}


