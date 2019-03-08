package com.threathunter.variable.json;

import com.threathunter.model.PropertyMapping;
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
 * @author Wen Lu
 */
public class PropertyMappingJsonDeserializer extends StdDeserializer<PropertyMapping> {

    private static final TypeToClassRegistry<PropertyMapping> registry =
            new TypeToClassRegistry<>(getParentPackageName(PropertyMappingJsonDeserializer.class),
                    "propertymapping.packages", PropertyMapping.class);

    protected PropertyMappingJsonDeserializer() {
        super(PropertyMapping.class);
    }

    @Override
    public PropertyMapping deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(jp);
        Class<? extends PropertyMapping> mappingClass = null;
        Iterator<Map.Entry<String, JsonNode>> elementsIterator =
                root.getFields();
        while (elementsIterator.hasNext())
        {
            Map.Entry<String, JsonNode> element=elementsIterator.next();
            String name = element.getKey();
            if (name.equals("type")) {
                String type = element.getValue().getTextValue();
                mappingClass = registry.getTypeClass(type);
                break;
            }
        }
        if (mappingClass == null)
            throw new NotSupportException("this mapping is not supported");
        return mapper.readValue(root, mappingClass);
    }

}
