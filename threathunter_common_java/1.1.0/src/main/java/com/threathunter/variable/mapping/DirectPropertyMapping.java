package com.threathunter.variable.mapping;

import com.threathunter.common.Identifier;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Directly mapping the property to a new property.
 *
 * Usually we just use property from existing event/variable as a property of another event/variable.
 *
 * @author Wen Lu
 */
public class DirectPropertyMapping extends PropertyMapping {
    public static final String TYPE = "direct";
    static {
        PropertyMapping.addSubClass(TYPE, DirectPropertyMapping.class);
    }

    // for json
    protected DirectPropertyMapping() {}

    public DirectPropertyMapping(Property src, Property dest) {
        super(Arrays.asList(src), dest, TYPE);
    }

    public DirectPropertyMapping(Property property) {
        super(Arrays.asList(property), property.deepCopy(), TYPE);
        getDestProperty().setIdentifier(null);
    }

    public DirectPropertyMapping(Property property, Identifier newIdentifier, String newName) {
        this(property);
        this.getDestProperty().setIdentifier(newIdentifier);
        this.getDestProperty().setName(newName);
    }

    public DirectPropertyMapping(Property property, String newName) {
        this(property);
        this.getDestProperty().setIdentifier(null);
        this.getDestProperty().setName(newName);
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("srcProperty", getSrcProperties().get(0).to_json_object());
        result.put("destProperty", getDestProperty().to_json_object());
        return result;
    }

    public static DirectPropertyMapping from_json_object(Object obj) {
        Map<Object, Object> map = (Map<Object, Object>)obj;

        Property src = Property.from_json_object(map.get("srcProperty"));
        Property dest = Property.from_json_object(map.get("destProperty"));
        dest.setType(src.getType());
        return new DirectPropertyMapping(src, dest);
    }
}
