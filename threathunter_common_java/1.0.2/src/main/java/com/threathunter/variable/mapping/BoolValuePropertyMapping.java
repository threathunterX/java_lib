package com.threathunter.variable.mapping;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Use a given bool for a new property.
 *
 * @author Wen Lu
 */
public class BoolValuePropertyMapping extends PropertyMapping {
    public static final String TYPE = "bool";
    static {
        PropertyMapping.addSubClass(TYPE, BoolValuePropertyMapping.class);
    }

    private boolean param;

    // for json
    protected BoolValuePropertyMapping() {}

    public BoolValuePropertyMapping(boolean param, Property destProperty) {
        super(new ArrayList<Property>(), destProperty, TYPE);
        if (destProperty.getType() != NamedType.BOOLEAN)
            throw new IllegalArgumentException("the dest property is not boolean type");
        this.param = param;
    }

    public BoolValuePropertyMapping(boolean param, String newPropertyName) {
        this(param, Property.buildBooleanProperty(null, newPropertyName));
    }

    public boolean getParam() {
        return param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BoolValuePropertyMapping that = (BoolValuePropertyMapping) o;

        return param == that.param;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (param ? 1 : 0);
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

    public static BoolValuePropertyMapping from_json_object(Object obj) {
        Map<Object, Object> map = (Map<Object, Object>)obj;

        Boolean param = (Boolean)map.get("param");
        Property p = Property.from_json_object(map.get("destProperty"));
        p.setType(NamedType.BOOLEAN);
        return new BoolValuePropertyMapping(param, p);
    }
}
