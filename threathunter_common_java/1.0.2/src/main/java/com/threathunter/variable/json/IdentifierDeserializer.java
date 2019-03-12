package com.threathunter.variable.json;

import com.threathunter.common.Identifier;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * created by www.threathunter.cn
 */
public class IdentifierDeserializer extends StdDeserializer<Identifier> {

    protected IdentifierDeserializer() {
        super(Identifier.class);
    }

    @Override
    public Identifier deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            keys.add(node.get(i).asText());
        }
        return Identifier.fromKeys(keys);
    }

}
