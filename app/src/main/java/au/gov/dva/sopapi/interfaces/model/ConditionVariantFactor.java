package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.interfaces.JsonSerializable;

public interface ConditionVariantFactor extends JsonSerializable {
    String getSubParagraph();
    String getText();
}
