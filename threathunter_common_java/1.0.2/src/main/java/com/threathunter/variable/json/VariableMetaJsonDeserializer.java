package com.threathunter.variable.json;

import com.threathunter.model.VariableMeta;
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
public class VariableMetaJsonDeserializer extends StdDeserializer<VariableMeta> {

    private static final TypeToClassRegistry<VariableMeta> registry =
            new TypeToClassRegistry<VariableMeta>(getParentPackageName(VariableMetaJsonDeserializer.class),
                    "variable.packages", VariableMeta.class);

    public VariableMetaJsonDeserializer() {
        super(VariableMeta.class);
    }

    @Override
    public VariableMeta deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(jp);
        Class<? extends VariableMeta> variableClass = null;
        Iterator<Map.Entry<String, JsonNode>> elementsIterator =
                root.getFields();
        while (elementsIterator.hasNext())
        {
            Map.Entry<String, JsonNode> element=elementsIterator.next();
            String name = element.getKey();
            if (name.equals("type")) {
                String type = element.getValue().getTextValue();
                variableClass = registry.getTypeClass(type);
                break;
            }
        }
        if (variableClass == null)
            throw new NotSupportException("this variable is not supported");
        return mapper.readValue(root, variableClass);
    }

}
