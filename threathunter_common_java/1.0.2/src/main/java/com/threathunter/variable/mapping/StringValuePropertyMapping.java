package com.threathunter.variable.mapping;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Use a given string for a new property.
 *
 * @author Wen Lu
 */
public class StringValuePropertyMapping extends PropertyMapping {
    public static final String TYPE = "string";
    static {
        PropertyMapping.addSubClass(TYPE, StringValuePropertyMapping.class);
    }

    private String param;

    // for json
    protected StringValuePropertyMapping() {}

    public StringValuePropertyMapping(String param, Property destProperty) {
        super(new ArrayList<Property>(), destProperty, TYPE);
        if (destProperty.getType() != NamedType.STRING)
            throw new IllegalArgumentException("the dest type is not string");
        if (param == null) {
            throw new RuntimeException("the param is null");
        }
        this.param = param;
    }

    public StringValuePropertyMapping(String param, String newPropertyName) {
        this(param, Property.buildStringProperty(null, newPropertyName));
    }

    public String getParam() {
        return param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        StringValuePropertyMapping that = (StringValuePropertyMapping) o;

        return param.equals(that.param);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + param.hashCode();
        return result;
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("destProperty", getDestProperty().to_json_object());
        result.put("param", param);
        return result;
    }

    public static StringValuePropertyMapping from_json_object(Object obj) {
        Map<Object, Object> map = (Map<Object, Object>)obj;

        String param = (String)map.get("param");
        Property p = Property.from_json_object(map.get("destProperty"));
        p.setType(NamedType.STRING);
        return new StringValuePropertyMapping(param, p);
    }
}
