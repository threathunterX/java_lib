package com.threathunter.variable.mapping;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concat multiple properties into a new string property.
 *
 * Usually we want to concat the string representation of multiple source properties into one string for use.
 *
 * @author Wen Lu
 */
public class ConcatPropertyMapping extends PropertyMapping {
    public static final String TYPE = "concat";
    static {
        PropertyMapping.addSubClass(TYPE, ConcatPropertyMapping.class);
    }

    // for json
    protected ConcatPropertyMapping() {}

    public ConcatPropertyMapping(List<Property> srcProperties, Property destProperty) {
        super(srcProperties, destProperty, TYPE);

        if (destProperty.getType() != NamedType.STRING)
            throw new RuntimeException("the dest type is not string");
    }

    public ConcatPropertyMapping(List<Property> srcProperties, String destPropertyName) {
        super(srcProperties, Property.buildStringProperty(destPropertyName), TYPE);
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        List<Object> srcPropertiesObject = new ArrayList<>();
        for(Property p : getSrcProperties()) {
            srcPropertiesObject.add(p.to_json_object());
        }
        result.put("type", getType());
        result.put("srcProperties", srcPropertiesObject);
        result.put("destProperty", getDestProperty().to_json_object());
        return result;
    }

    public static ConcatPropertyMapping from_json_object(Object obj) {
        Map<Object, Object> map = (Map<Object, Object>)obj;

        List<Object> srcPropertiesObject = (List<Object>)(map.get("srcProperties"));
        List<Property> srcProperties = new ArrayList<>();
        for(Object o : srcPropertiesObject) {
            srcProperties.add(Property.from_json_object(o));
        }
        Property dest = Property.from_json_object(map.get("destProperty"));
        dest.setType(NamedType.STRING);
        return new ConcatPropertyMapping(srcProperties, dest);
    }
    
}
