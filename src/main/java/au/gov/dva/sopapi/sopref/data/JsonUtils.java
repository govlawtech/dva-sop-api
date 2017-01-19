package au.gov.dva.sopapi.sopref.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonUtils {

    public static ImmutableList<JsonNode> getChildrenOfArrayNode(JsonNode jsonNode)
    {
        assert (jsonNode.isArray());
        List<JsonNode> children = new ArrayList<>();

        for (Iterator<JsonNode> it = jsonNode.elements(); it.hasNext(); ) {
            JsonNode el = it.next();
            children.add(el);
        }

        return ImmutableList.copyOf(children);
    }
}
