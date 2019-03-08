package com.threathunter.variable.condition;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyCondition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daisy on 16-1-18.
 */
public abstract class IPPropertyCondition extends SimplePropertyCondition {
    // paramType: city/province
    private String paramType;
    // rightParams: [Sichuan, Chengdu]
    private String[] rightParams;

    private Map<String, Object> paramJson;

    protected IPPropertyCondition() {}

    public IPPropertyCondition(Property property, String paramType, String[] rightParams, String type) {
        super(property, type, rightParams);
        if (property.getType() != NamedType.STRING) {
            throw new IllegalArgumentException("the property is not string type");
        }
        this.paramType = paramType;
        this.rightParams = rightParams;
        paramJson = new HashMap<>();
        paramJson.put("type", paramType);
        paramJson.put("params", rightParams);
    }

    public IPPropertyCondition(String propertyName, String paramType, String[] rightParams, String type) {
        this(Property.buildStringProperty(propertyName), paramType, rightParams, type);
    }

    public String[] getRightParams() {
        return rightParams;
    }
    public String getParamType() {
        return paramType;
    }

    @Override
    public Object getParam() {
        return this.paramJson;
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("srcProperty", getSrcProperties().get(0).to_json_object());
        result.put("rightParam", rightParams);
        result.put("paramType", paramType);
        return result;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPPropertyCondition that = (IPPropertyCondition) o;

        if (!getType().equals(that.getType())) return false;
        if (!paramType.equals(that.paramType)) return false;
        return Arrays.equals(rightParams, that.rightParams);

    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + paramType.hashCode();
        result = 31 * result + rightParams.hashCode();
        return result;
    }

    public static class IPEqualsPropertyCondition extends IPPropertyCondition {
        public static final String TYPE = "stringlocationequals";
        static {
            PropertyCondition.addSubClass(TYPE, IPEqualsPropertyCondition.class);
        }

        protected IPEqualsPropertyCondition() {}

        public IPEqualsPropertyCondition(Property property, String paramType, String[] rightParams) {
            super(property, paramType, rightParams, TYPE);
        }

        public IPEqualsPropertyCondition(String propertyName, String paramType, String[] rightParams) {
            super(propertyName, paramType, rightParams, TYPE);
        }

        public static IPEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String paramType = (String)map.get("paramType");
            String[] rightParam = ((String)map.get("param")).split(",");
            return new IPEqualsPropertyCondition(p, paramType, rightParam);
        }
    }
    public static class IPNotEqualsPropertyCondition extends IPPropertyCondition {
        public static final String TYPE = "string!locationequals";
        static {
            PropertyCondition.addSubClass(TYPE, IPNotEqualsPropertyCondition.class);
        }

        protected IPNotEqualsPropertyCondition() {}

        public IPNotEqualsPropertyCondition(Property property, String paramType, String[] rightParams) {
            super(property, paramType, rightParams, TYPE);
        }

        public IPNotEqualsPropertyCondition(String propertyName, String paramType, String[] rightParams) {
            super(propertyName, paramType, rightParams, TYPE);
        }

        public static IPNotEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String paramType = (String)map.get("paramType");
            String[] rightParam = ((String)map.get("param")).split(",");
            return new IPNotEqualsPropertyCondition(p, paramType, rightParam);
        }
    }
    public static class IPContainsPropertyCondition extends IPPropertyCondition {
        public static final String TYPE = "stringlocationcontainsby";
        static {
            PropertyCondition.addSubClass(TYPE, IPContainsPropertyCondition.class);
        }

        protected IPContainsPropertyCondition() {}

        public IPContainsPropertyCondition(Property property, String paramType, String[] rightParams) {
            super(property, paramType, rightParams, TYPE);
        }

        public IPContainsPropertyCondition(String propertyName, String paramType, String[] rightParams) {
            super(propertyName, paramType, rightParams, TYPE);
        }

        public static IPContainsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String paramType = (String)map.get("paramType");
            String[] rightParam = ((String)map.get("param")).split(",");
            return new IPContainsPropertyCondition(p, paramType, rightParam);
        }
    }
    public static class IPNotContainsPropertyCondition extends IPPropertyCondition {
        public static final String TYPE = "string!locationcontainsby";
        static {
            PropertyCondition.addSubClass(TYPE, IPNotContainsPropertyCondition.class);
        }

        protected IPNotContainsPropertyCondition() {}

        public IPNotContainsPropertyCondition(Property property, String paramType, String[] rightParams) {
            super(property, paramType, rightParams, TYPE);
        }

        public IPNotContainsPropertyCondition(String propertyName, String paramType, String[] rightParams) {
            super(propertyName, paramType, rightParams, TYPE);
        }

        public static IPNotContainsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String paramType = (String)map.get("paramType");
            String[] rightParam = ((String)map.get("param")).split(",");
            return new IPNotContainsPropertyCondition(p, paramType, rightParam);
        }
    }
}
