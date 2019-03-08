package com.threathunter.variable.mapping;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daisy on 16/6/9.
 */
public class LocationPropertyMapping extends PropertyMapping {
    public static final String TYPE = "location";
    static {
        PropertyMapping.addSubClass(TYPE, LocationPropertyMapping.class);
    }

    // param indicate the filed to get the geo location, like c_ip, mobile
    private Property srcProperty;
    // type is the geo type, include city, province, all, all will return province city
    private String geoType;

    protected LocationPropertyMapping() {}

    public LocationPropertyMapping(Property srcProperty, Property destProperty, String geoType) {
        super(new ArrayList<Property>(), destProperty, TYPE);
        if (destProperty.getType() != NamedType.STRING)
            throw new IllegalArgumentException("the dest type is not string");
        if (srcProperty == null) {
            throw new RuntimeException("src property is null");
        }
        this.srcProperty = srcProperty;
        this.geoType = geoType;
    }

    public LocationPropertyMapping(Property srcProperty, String geoType, String newPropertyName) {
        this(srcProperty, Property.buildStringProperty(null, newPropertyName), geoType);
    }

    public Property getSrcProperty() {
        return srcProperty;
    }

    public String getGeoType() {
        return geoType;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + geoType.hashCode();
        return result;
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("srcProperty", getSrcProperty().to_json_object());
        result.put("destProperty", getDestProperty().to_json_object());
        result.put("geoType", geoType);
        return result;
    }

    public static LocationPropertyMapping from_json_object(Object obj) {
        Map<Object, Object> map = (Map<Object, Object>)obj;

        String geoType = (String)map.get("geoType");
        Property src = Property.from_json_object(map.get("srcProperty"));
        Property dest = Property.from_json_object(map.get("destProperty"));
        dest.setType(NamedType.STRING);
        return new LocationPropertyMapping(src, dest, geoType);
    }
}
