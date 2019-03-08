package com.threathunter.variable.mapping;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Use a given double long for a new property.
 *
 * @author Wen Lu
 */
public class DoubleValuePropertyMapping extends PropertyMapping {
    public static final String TYPE = "double";
    static {
        PropertyMapping.addSubClass(TYPE, DoubleValuePropertyMapping.class);
    }

    private double param;

    // for json
    protected DoubleValuePropertyMapping() {}

    public DoubleValuePropertyMapping(double param, Property destProperty) {
        super(new ArrayList<Property>(), destProperty, TYPE);
        if (destProperty.getType() != NamedType.DOUBLE)
            throw new IllegalArgumentException("the dest type is not double");
        this.param = param;
    }

    public DoubleValuePropertyMapping(double param, String newPropertyName) {
        this(param, Property.buildDoubleProperty(null, newPropertyName));
    }

    public double getParam() {
        return param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DoubleValuePropertyMapping that = (DoubleValuePropertyMapping) o;

        return Double.compare(that.param, param) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(param);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
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

    public static DoubleValuePropertyMapping from_json_object(Object obj) {
        Map<Object, Object> map = (Map<Object, Object>)obj;

        Number param = (Number)map.get("param");
        Property p = Property.from_json_object(map.get("destProperty"));
        p.setType(NamedType.DOUBLE);
        return new DoubleValuePropertyMapping(param.doubleValue(), p);
    }
}
