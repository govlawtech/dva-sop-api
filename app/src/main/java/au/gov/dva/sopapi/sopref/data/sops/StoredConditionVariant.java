package au.gov.dva.sopapi.sopref.data.sops;

import au.gov.dva.sopapi.interfaces.JsonSerializable;
import au.gov.dva.sopapi.interfaces.model.ConditionVariant;
import au.gov.dva.sopapi.interfaces.model.ConditionVariantFactor;
import au.gov.dva.sopapi.sopref.data.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StoredConditionVariant implements ConditionVariant, JsonSerializable {

    public static final String CONDITION_VARIANT_NAME_LABEL = "name";
    public static final String CONDITION_VARIANT_FACTORS_LABEL = "variantFactors";

    private final String name;
    private final ImmutableList<ConditionVariantFactor> factors;

    public StoredConditionVariant(String name, ImmutableList<ConditionVariantFactor> factors)
    {

        this.name = name;
        this.factors = factors;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableList<ConditionVariantFactor> getVariantFactors() {
        return factors;
    }

    @Override
    public JsonNode toJson() {
       return toJson(this);
    }

    public static JsonNode toJson(ConditionVariant conditionVariant)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode root = objectMapper.createObjectNode();
        root.put(CONDITION_VARIANT_NAME_LABEL,conditionVariant.getName());
        ArrayNode variantFactors = root.putArray(CONDITION_VARIANT_FACTORS_LABEL);
        for (ConditionVariantFactor cf : conditionVariant.getVariantFactors())
        {
            variantFactors.add(StoredConditionVariantFactor.toJson(cf));
        }
        return root;
    }

    public static ConditionVariant fromJson(JsonNode jsonNode)
    {
     //   assert(jsonNode.has(CONDITION_VARIANT_FACTORS_LABEL) && jsonNode.findPath(CONDITION_VARIANT_FACTORS_LABEL).isArray());
        String name = jsonNode.findValue(CONDITION_VARIANT_NAME_LABEL).asText();
        ImmutableList<JsonNode> factors = JsonUtils.getChildrenOfArrayNode(jsonNode.findPath(CONDITION_VARIANT_FACTORS_LABEL));
        ImmutableList<ConditionVariantFactor> deserialisedFactors = factors.stream().map(StoredConditionVariantFactor::fromJson)
                .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));
        return new StoredConditionVariant(name,deserialisedFactors);
    }

}
