package com.threathunter.variable.json;

import com.threathunter.common.Identifier;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * @author Wen Lu
 */
public class IdentifierSerializer extends JsonSerializer<Identifier> {
    @Override
    public void serialize(Identifier identifier, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (identifier == null)
            return;

        jsonGenerator.writeObject(identifier.getKeys());
    }
}
