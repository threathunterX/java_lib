package com.threathunter.variable.condition;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyCondition;

import java.util.HashMap;
import java.util.Map;

import static com.threathunter.common.Utility.argumentNotEmpty;

/**
 * A property condition that is comparing the field of type {code LONG} with another long value.
 *
 * @author Wen Lu
 */
public abstract class LongPropertyCondition extends SimplePropertyCondition {
    private Long param;

    // for json
    protected LongPropertyCondition() {}

    public LongPropertyCondition(Property property, Long param, String type) {
        super(property, type, param);
        if (property.getType() != NamedType.LONG) {
            throw new IllegalArgumentException("the property is not long type");
        }

        argumentNotEmpty(param, "null param");
        this.param = param;
    }

    public LongPropertyCondition(String propertyName, Long param, String type) {
        this(Property.buildLongProperty(propertyName), param, type);
    }

    public void setParam(Long param) {
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

        LongPropertyCondition that = (LongPropertyCondition) o;

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

    public static class LongDistinctPropertyCondition extends LongPropertyCondition {
        public static final String TYPE = "longdistinct_count";
        static {
            PropertyCondition.addSubClass(TYPE, LongDistinctPropertyCondition.class);
        }

        // for json
        protected LongDistinctPropertyCondition() {}

        public LongDistinctPropertyCondition(Property property, long data) {
            super(property, data, TYPE);
        }

        public LongDistinctPropertyCondition(String propertyName, long data) {
            super(propertyName, data, TYPE);
        }

        public static LongDistinctPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            long data = Long.valueOf((String) map.get("param"));
            return new LongDistinctPropertyCondition(p, data);
        }
    }

    public static class LongSmallerThanPropertyCondition extends LongPropertyCondition {
        public static final String TYPE = "long<";
        static {
            PropertyCondition.addSubClass(TYPE, LongSmallerThanPropertyCondition.class);
        }

        // for json
        protected LongSmallerThanPropertyCondition() {}

        public LongSmallerThanPropertyCondition(Property property, long threshold) {
            super(property, threshold, TYPE);
        }

        public LongSmallerThanPropertyCondition(String propertyName, long threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static LongSmallerThanPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            long threshold = Long.valueOf((String) map.get("param"));
            return new LongSmallerThanPropertyCondition(p, threshold);
        }
    }

    public static class LongBiggerThanPropertyCondition extends LongPropertyCondition {
        public static final String TYPE = "long>";
        static {
            PropertyCondition.addSubClass(TYPE, LongBiggerThanPropertyCondition.class);
        }

        // for json
        protected LongBiggerThanPropertyCondition() {}

        public LongBiggerThanPropertyCondition(Property property, long threshold) {
            super(property, threshold, TYPE);
        }

        public LongBiggerThanPropertyCondition(String propertyName, long threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static LongBiggerThanPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            long threshold = Long.valueOf((String) map.get("param"));
            return new LongBiggerThanPropertyCondition(p, threshold);
        }
    }

    public static class LongEqualsPropertyCondition extends LongPropertyCondition {
        public static final String TYPE = "long==";
        static {
            PropertyCondition.addSubClass(TYPE, LongEqualsPropertyCondition.class);
        }

        // for json
        protected LongEqualsPropertyCondition() {}

        public LongEqualsPropertyCondition(Property property, long threshold) {
            super(property, threshold, TYPE);
        }

        public LongEqualsPropertyCondition(String propertyName, long threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static LongEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            long threshold = Long.valueOf((String) map.get("param"));
            return new LongEqualsPropertyCondition(p, threshold);
        }
    }

    public static class LongNotEqualsPropertyCondition extends LongPropertyCondition {
        public static final String TYPE = "long!=";
        static {
            PropertyCondition.addSubClass(TYPE, LongNotEqualsPropertyCondition.class);
        }

        // for json
        protected LongNotEqualsPropertyCondition() {}

        public LongNotEqualsPropertyCondition(Property property, long threshold) {
            super(property, threshold, TYPE);
        }

        public LongNotEqualsPropertyCondition(String propertyName, long threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static LongNotEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            long threshold = Long.valueOf((String) map.get("param"));
            return new LongNotEqualsPropertyCondition(p, threshold);
        }
    }

    public static class LongSmallerEqualsPropertyCondition extends LongPropertyCondition {
        public static final String TYPE = "long<=";
        static {
            PropertyCondition.addSubClass(TYPE, LongSmallerEqualsPropertyCondition.class);
        }

        // for json
        protected LongSmallerEqualsPropertyCondition() {}

        public LongSmallerEqualsPropertyCondition(Property property, long threshold) {
            super(property, threshold, TYPE);
        }

        public LongSmallerEqualsPropertyCondition(String propertyName, long threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static LongSmallerEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            long threshold = Long.valueOf((String) map.get("param"));
            return new LongSmallerEqualsPropertyCondition(p, threshold);
        }
    }

    public static class LongBiggerEqualsPropertyCondition extends LongPropertyCondition {
        public static final String TYPE = "long>=";
        static {
            PropertyCondition.addSubClass(TYPE, LongBiggerEqualsPropertyCondition.class);
        }

        // for json
        protected LongBiggerEqualsPropertyCondition() {}

        public LongBiggerEqualsPropertyCondition(Property property, long threshold) {
            super(property, threshold, TYPE);
        }

        public LongBiggerEqualsPropertyCondition(String propertyName, long threshold) {
            super(propertyName, threshold, TYPE);
        }

        public static LongBiggerEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            long threshold = Long.valueOf((String) map.get("param"));
            return new LongBiggerEqualsPropertyCondition(p, threshold);
        }
    }
}
