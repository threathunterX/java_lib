package com.threathunter.variable.condition;

import com.threathunter.model.Property;
import com.threathunter.model.PropertyCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * General condition that user need to give the condition implementation themselves.
 *
 * @author Wen Lu
 */
public class GeneralPropertyCondition extends PropertyCondition {
    public static final String TYPE = "general";
    static {
        PropertyCondition.addSubClass(TYPE, GeneralPropertyCondition.class);
    }

    private List<Property> srcProperties;
    private String config;

    // for json
    protected GeneralPropertyCondition() {
    }

    public GeneralPropertyCondition(List<Property> srcProperties, String config) {
        super(TYPE);
        this.srcProperties = srcProperties;
        this.config = config;
    }

    @Override
    public Object getParam() {
        return this.config;
    }

    @Override
    public List<Property> getSrcProperties() {
        return srcProperties;
    }

    public String getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralPropertyCondition that = (GeneralPropertyCondition) o;

        if (!srcProperties.equals(that.srcProperties)) return false;
        return config.equals(that.config);

    }

    @Override
    public int hashCode() {
        int result = srcProperties.hashCode();
        result = 31 * result + config.hashCode();
        return result;
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        List<Object> srcObjects = new ArrayList<>();
        for(Property src : srcProperties) {
            srcObjects.add(src.to_json_object());
        }

        result.put("type", getType());
        result.put("srcProperties", srcObjects);
        result.put("config", config);
        return result;
    }

    public static GeneralPropertyCondition from_json_object(Object obj) {
        Map<Object, Object> map = (Map<Object, Object>)obj;
        List<Object> srcObjects = (List<Object>)map.get("srcProperties");

        String config = (String)map.get("config");
        List<Property> srcProperties = new ArrayList<>();
        for(Object o : srcObjects) {
            srcProperties.add(Property.from_json_object(o));
        }
        return new GeneralPropertyCondition(srcProperties, config);
    }
}
