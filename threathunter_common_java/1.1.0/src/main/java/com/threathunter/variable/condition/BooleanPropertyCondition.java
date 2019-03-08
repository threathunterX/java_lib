package com.threathunter.variable.condition;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyCondition;

import java.util.Map;

import static com.threathunter.common.Utility.argumentNotEmpty;

/**
 * Created by daisy on 18-3-15
 */
public abstract class BooleanPropertyCondition extends SimplePropertyCondition {
    private Boolean param;

    protected BooleanPropertyCondition() {

    }

    public BooleanPropertyCondition(Property property, Boolean param, String type) {
        super(property, type, param);
        if (property.getType() != NamedType.BOOLEAN) {
            throw new IllegalArgumentException("the property is not boolean type");
        }

        argumentNotEmpty(param, "null param");
        this.param = param;
    }

    public BooleanPropertyCondition(String propertyName, Boolean param, String type) {
        this(Property.buildBooleanProperty(propertyName), param, type);
    }

    @Override
    public Object getParam() {
        return this.param;
    }

    @Override
    public Object to_json_object() {
        return "";
    }

    @Override
    public int hashCode() {
        return param.hashCode() * 31 + getType().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BooleanPropertyCondition that = (BooleanPropertyCondition) o;

        if (!getType().equals(that.getType())) return false;
        return param.equals(that.param);
    }

    public static class BooleanEqualsPropertyCondition extends BooleanPropertyCondition {
        public static final String TYPE = "boolean==";
        static {
            PropertyCondition.addSubClass(TYPE, BooleanEqualsPropertyCondition.class);
        }

        // for json
        protected BooleanEqualsPropertyCondition() {}

        public BooleanEqualsPropertyCondition(Property property, boolean param) {
            super(property, param, TYPE);
        }

        public BooleanEqualsPropertyCondition(String propertyName, boolean param) {
            super(propertyName, param, TYPE);
        }

        public static BooleanEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            boolean param = Boolean.valueOf((String) map.get("param"));
            return new BooleanEqualsPropertyCondition(p, param);
        }
    }

    public static class BooleanNotEqualsPropertyCondition extends BooleanPropertyCondition {
        public static final String TYPE = "boolean!=";
        static {
            PropertyCondition.addSubClass(TYPE, BooleanNotEqualsPropertyCondition.class);
        }

        // for json
        protected BooleanNotEqualsPropertyCondition() {}

        public BooleanNotEqualsPropertyCondition(Property property, boolean param) {
            super(property, param, TYPE);
        }

        public BooleanNotEqualsPropertyCondition(String propertyName, boolean param) {
            super(propertyName, param, TYPE);
        }

        public static BooleanNotEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            boolean param = Boolean.valueOf((String) map.get("param"));
            return new BooleanNotEqualsPropertyCondition(p, param);
        }
    }
}
