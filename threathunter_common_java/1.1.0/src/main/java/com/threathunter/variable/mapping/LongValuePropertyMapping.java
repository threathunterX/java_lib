package com.threathunter.variable.mapping;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Use a given long for a new property.
 *
 * created by www.threathunter.cn
 */
public class LongValuePropertyMapping extends PropertyMapping {
    public static final String TYPE = "long";
    static {
        PropertyMapping.addSubClass(TYPE, LongValuePropertyMapping.class);
    }

    private long param;

    // for json
    protected LongValuePropertyMapping() {}

    public LongValuePropertyMapping(long param, Property destProperty) {
        super(new ArrayList<Property>(), destProperty, TYPE);
        if (destProperty.getType() != NamedType.LONG)
            throw new IllegalArgumentException("the dest type is not long");
        this.param = param;
    }

    public LongValuePropertyMapping(long param, String newPropertyName) {
        this(param, Property.buildLongProperty(null, newPropertyName));
    }

    public long getParam() {
        return param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LongValuePropertyMapping that = (LongValuePropertyMapping) o;

        return param == that.param;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (param ^ (param >>> 32));
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

    public static LongValuePropertyMapping from_json_object(Object obj) {
        Map<Object, Object> map = (Map<Object, Object>)obj;

        Number param = (Number)map.get("param");
        Property p = Property.from_json_object(map.get("destProperty"));
        p.setType(NamedType.LONG);
        return new LongValuePropertyMapping(param.longValue(), p);
    }
}
