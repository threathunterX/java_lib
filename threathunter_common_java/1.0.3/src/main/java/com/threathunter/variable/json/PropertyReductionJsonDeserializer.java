package com.threathunter.variable.json;

import com.threathunter.model.PropertyReduction;
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
public class PropertyReductionJsonDeserializer extends StdDeserializer<PropertyReduction> {

    private static final TypeToClassRegistry<PropertyReduction> registry =
            new TypeToClassRegistry<>(getParentPackageName(PropertyReductionJsonDeserializer.class),
                    "propertyreduction.packages", PropertyReduction.class);

    protected PropertyReductionJsonDeserializer() {
        super(PropertyReduction.class);
    }

    @Override
    public PropertyReduction deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(jp);
        Class<? extends PropertyReduction> reductionClass = null;
        Iterator<Map.Entry<String, JsonNode>> elementsIterator =
                root.getFields();
        while (elementsIterator.hasNext())
        {
            Map.Entry<String, JsonNode> element=elementsIterator.next();
            String name = element.getKey();
            if (name.equals("type")) {
                String type = element.getValue().getTextValue();
                reductionClass = registry.getTypeClass(type);
                break;
            }
        }
        if (reductionClass == null)
            throw new NotSupportException("this reduction is not supported");
        return mapper.readValue(root, reductionClass);
    }

}
