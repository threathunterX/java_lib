package com.threathunter.variable.condition;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyCondition;

import java.util.HashMap;
import java.util.Map;

import static com.threathunter.common.Utility.argumentNotEmpty;

/**
 * A property condition that is comparing the field of type {code DOUBLE} with another double value.
 *
 * created by www.threathunter.cn
 */
public abstract class DoublePropertyCondition extends SimplePropertyCondition {
    private Double param;

    // for json
    protected DoublePropertyCondition() {}

    public DoublePropertyCondition(Property property, Double param, String type) {
        super(property, type, param);
        if (property.getType() != NamedType.DOUBLE)
            throw new IllegalArgumentException("the property is not double");

        argumentNotEmpty(param, "null param");
        this.param = param;
    }

    public DoublePropertyCondition(String propertyName, Double param, String type) {
        this(Property.buildDoubleProperty(null, propertyName), param, type);
    }

    public void setParam(Double param) {
        argumentNotEmpty(param, "null param");
        this.param = param;
    }

    @Override
    public Object getParam() {
        return this.param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoublePropertyCondition that = (DoublePropertyCondition) o;

        if (!getType().equals(that.getType())) return false;
        return param.equals(that.param);

    }

    @Override
    public int hashCode() {
        return param.hashCode() * 31 + getType().hashCode();
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("srcProperty", getSrcProperties().get(0).to_json_object());
        result.put("param", param);
        return result;
    }

    public static class DoubleSmallerThanPropertyCondition extends DoublePropertyCondition {
        public static final String TYPE = "double<";
        static {
            PropertyCondition.addSubClass(TYPE, DoubleSmallerThanPropertyCondition.class);
        }

        // for json
        protected DoubleSmallerThanPropertyCondition() {}

        public DoubleSmallerThanPropertyCondition(Property property, double threshold) {
            super(property, threshold, TYPE);
        }

        public DoubleSmallerThanPropertyCondition(String propertyName, double threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static DoubleSmallerThanPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            double threshold = Double.valueOf((String) map.get("param"));
            return new DoubleSmallerThanPropertyCondition(p, threshold);
        }
    }

    public static class DoubleBiggerThanPropertyCondition extends DoublePropertyCondition {
        public static final String TYPE = "double>";
        static {
            PropertyCondition.addSubClass(TYPE, DoubleBiggerThanPropertyCondition.class);
        }

        // for json
        protected DoubleBiggerThanPropertyCondition() {}

        public DoubleBiggerThanPropertyCondition(Property property, double threshold) {
            super(property, threshold, TYPE);
        }

        public DoubleBiggerThanPropertyCondition(String propertyName, double threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static DoubleBiggerThanPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            double threshold = Double.valueOf((String) map.get("param"));
            return new DoubleBiggerThanPropertyCondition(p, threshold);
        }
    }

    public static class DoubleEqualsPropertyCondition extends DoublePropertyCondition {
        public static final String TYPE = "double==";
        static {
            PropertyCondition.addSubClass(TYPE, DoubleEqualsPropertyCondition.class);
        }

        // for json
        protected DoubleEqualsPropertyCondition() {}

        public DoubleEqualsPropertyCondition(Property property, double threshold) {
            super(property, threshold, TYPE);
        }

        public DoubleEqualsPropertyCondition(String propertyName, double threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static DoubleEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            double threshold = Double.valueOf((String) map.get("param"));
            return new DoubleEqualsPropertyCondition(p, threshold);
        }
    }

    public static class DoubleNotEqualsPropertyCondition extends DoublePropertyCondition {
        public static final String TYPE = "double!=";
        static {
            PropertyCondition.addSubClass(TYPE, DoubleNotEqualsPropertyCondition.class);
        }

        // for json
        protected DoubleNotEqualsPropertyCondition() {}

        public DoubleNotEqualsPropertyCondition(Property property, double threshold) {
            super(property, threshold, TYPE);
        }

        public DoubleNotEqualsPropertyCondition(String propertyName, double threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static DoubleNotEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            double threshold = Double.valueOf((String) map.get("param"));
            return new DoubleNotEqualsPropertyCondition(p, threshold);
        }
    }

    public static class DoubleSmallerEqualsPropertyCondition extends DoublePropertyCondition {
        public static final String TYPE = "double<=";
        static {
            PropertyCondition.addSubClass(TYPE, DoubleSmallerEqualsPropertyCondition.class);
        }

        // for json
        protected DoubleSmallerEqualsPropertyCondition() {}

        public DoubleSmallerEqualsPropertyCondition(Property property, double threshold) {
            super(property, threshold, TYPE);
        }

        public DoubleSmallerEqualsPropertyCondition(String propertyName, double threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static DoubleSmallerEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            double threshold = Double.valueOf((String) map.get("param"));
            return new DoubleSmallerEqualsPropertyCondition(p, threshold);
        }
    }

    public static class DoubleBiggerEqualsPropertyCondition extends DoublePropertyCondition {
        public static final String TYPE = "double>=";
        static {
            PropertyCondition.addSubClass(TYPE, DoubleBiggerEqualsPropertyCondition.class);
        }

        // for json
        protected DoubleBiggerEqualsPropertyCondition() {}

        public DoubleBiggerEqualsPropertyCondition(Property property, double threshold) {
            super(property, threshold, TYPE);
        }

        public DoubleBiggerEqualsPropertyCondition(String propertyName, double threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static DoubleBiggerEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            double threshold = Double.valueOf((String) map.get("param"));
            return new DoubleBiggerEqualsPropertyCondition(p, threshold);
        }
    }
}
