package com.threathunter.variable.json;

import com.threathunter.common.NamedType;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.deser.StdDeserializer;

import java.io.IOException;

/**
 * @author Wen Lu
 */
public class NamedTypeProcessor {
    public static class NamedTypeSerializer extends JsonSerializer<NamedType> {

        @Override
        public void serialize(NamedType value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
             if (value == null)
                 return;

            jgen.writeString(value.getCode());
        }
    }

    public static class NamedTypeDeserializer extends StdDeserializer<NamedType> {

        protected NamedTypeDeserializer() {
            super(NamedType.class);
        }

        @Override
        public NamedType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            ObjectCodec oc = jp.getCodec();
            JsonNode node = oc.readTree(jp);
            return NamedType.fromCode(node.asText());
        }
    }
}
