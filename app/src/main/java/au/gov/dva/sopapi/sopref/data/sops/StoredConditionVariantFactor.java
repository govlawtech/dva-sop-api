package au.gov.dva.sopapi.sopref.data.sops;

import au.gov.dva.sopapi.interfaces.JsonSerializable;
import au.gov.dva.sopapi.interfaces.model.ConditionVariant;
import au.gov.dva.sopapi.interfaces.model.ConditionVariantFactor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class StoredConditionVariantFactor implements ConditionVariantFactor, JsonSerializable {

    public static final String CONDITION_VARIANT_FACTOR_PARA_LABEL = "paragraph";
    public static final String CONDITION_VARIANT_FACTOR_TEXT_LABEL = "text";

    private final String subPara;
    private final String text;

    public StoredConditionVariantFactor(String subPara, String text) {
        this.subPara = subPara;
        this.text = text;
    }

    @Override
    public String getSubParagraph() {
        return subPara;
    }

    @Override
    public String getText() {
        return text;
    }

    public static ConditionVariantFactor fromJson(JsonNode jsonNode)
    {
        assert (jsonNode.has(CONDITION_VARIANT_FACTOR_TEXT_LABEL) && jsonNode.has(CONDITION_VARIANT_FACTOR_PARA_LABEL));
        return new StoredConditionVariantFactor(
                jsonNode.findValue(CONDITION_VARIANT_FACTOR_PARA_LABEL).asText(),
                jsonNode.findValue(CONDITION_VARIANT_FACTOR_TEXT_LABEL).asText()
        );
    }


    @Override
    public JsonNode toJson() {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode factorNode = objectMapper.createObjectNode();
        factorNode.put(CONDITION_VARIANT_FACTOR_PARA_LABEL,getSubParagraph());
        factorNode.put(CONDITION_VARIANT_FACTOR_TEXT_LABEL,getText());
        return factorNode;
    }


}
