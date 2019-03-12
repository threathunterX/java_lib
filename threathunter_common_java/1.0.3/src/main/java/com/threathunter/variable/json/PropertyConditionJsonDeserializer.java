package com.threathunter.variable.json;

import com.threathunter.model.PropertyCondition;
import com.threathunter.variable.exception.NotSupportException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static com.threathunter.common.Utility.getParentPackageName;

/**
 * created by www.threathunter.cn
 */
public class PropertyConditionJsonDeserializer extends StdDeserializer<PropertyCondition> {

    private static final TypeToClassRegistry<PropertyCondition> registry =
            new TypeToClassRegistry<>(getParentPackageName(PropertyConditionJsonDeserializer.class),
                    "propertycondition.packages", PropertyCondition.class);

    protected PropertyConditionJsonDeserializer() {
        super(PropertyCondition.class);
    }

    @Override
    public PropertyCondition deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(jp);
        Class<? extends PropertyCondition> conditionClass = null;
        Iterator<Map.Entry<String, JsonNode>> elementsIterator =
                root.getFields();
        while (elementsIterator.hasNext())
        {
            Map.Entry<String, JsonNode> element=elementsIterator.next();
            String name = element.getKey();
            if (name.equals("type")) {
                String type = element.getValue().getTextValue();
                conditionClass = registry.getTypeClass(type);
                break;
            }
        }
        if (conditionClass == null)
            throw new NotSupportException("this condition is not supported");
        return mapper.readValue(root, conditionClass);
    }

}
