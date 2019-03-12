package com.threathunter.variable.condition;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyCondition;

import java.util.HashMap;
import java.util.Map;

/**
 * A property condition that is comparing the field of type {code STRING} with another string value.
 *
 * created by www.threathunter.cn
 */
public abstract class StringPropertyCondition extends SimplePropertyCondition {
    private String param;

    // for json
    protected StringPropertyCondition() {}

    public StringPropertyCondition(Property property, String param, String type) {
        super(property, type, param);
        if (property.getType() != NamedType.STRING) {
            throw new IllegalArgumentException("the property is not strign type");
        }
        this.param = param;
    }

    public StringPropertyCondition(String propertyName, String param, String type) {
        this(Property.buildStringProperty(propertyName), param, type);
    }

    public void setParam(String param) {
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

        StringPropertyCondition that = (StringPropertyCondition) o;

        if (!getType().equals(that.getType())) return false;
        return param.equals(that.param);

    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + (param == null ? 0 : param.hashCode());
        return result;
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("srcProperty", getSrcProperties().get(0).to_json_object());
        result.put("param", param);
        return result;
    }

    public static class StringContainsPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "stringcontains";
        static {
            PropertyCondition.addSubClass(TYPE, StringContainsPropertyCondition.class);
        }

        // for json
        protected StringContainsPropertyCondition(){
        }

        public StringContainsPropertyCondition(Property property, String contains) {
            super(property, contains, TYPE);
        }

        public StringContainsPropertyCondition(String propertyName, String contains) {
            super(propertyName, contains, TYPE);
        }

        public static StringContainsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringContainsPropertyCondition(p, param);
        }
    }

    public static class StringNotContainsPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "string!contains";
        static {
            PropertyCondition.addSubClass(TYPE, StringNotContainsPropertyCondition.class);
        }

        // for json
        protected StringNotContainsPropertyCondition() {}

        public StringNotContainsPropertyCondition(Property property, String contains) {
            super(property, contains, TYPE);
        }

        public StringNotContainsPropertyCondition(String propertyName, String contains) {
            super(propertyName, contains, TYPE);
        }

        public static StringNotContainsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringNotContainsPropertyCondition(p, param);
        }
    }

    public static class StringContainsByPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "stringcontainsby";
        static {
            PropertyCondition.addSubClass(TYPE, StringContainsByPropertyCondition.class);
        }

        // for json
        protected StringContainsByPropertyCondition() {}

        public StringContainsByPropertyCondition(Property property, String containsBy) {
            super(property, containsBy, TYPE);
        }

        public StringContainsByPropertyCondition(String propertyName, String containsBy) {
            super(propertyName, containsBy, TYPE);
        }

        public static StringContainsByPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringContainsByPropertyCondition(p, param);
        }
    }

    public static class StringNotContainsByPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "string!containsby";
        static {
            PropertyCondition.addSubClass(TYPE, StringNotContainsByPropertyCondition.class);
        }

        // for json
        protected StringNotContainsByPropertyCondition() {}

        public StringNotContainsByPropertyCondition(Property property, String containsBy) {
            super(property, containsBy, TYPE);
        }

        public StringNotContainsByPropertyCondition(String propertyName, String containsBy) {
            super(propertyName, containsBy, TYPE);
        }

        public static StringNotContainsByPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringNotContainsByPropertyCondition(p, param);
        }
    }

    public static class StringEqualsPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "string==";
        static {
            PropertyCondition.addSubClass(TYPE, StringEqualsPropertyCondition.class);
        }

        // for json
        protected StringEqualsPropertyCondition() {}

        public StringEqualsPropertyCondition(Property property, String equals) {
            super(property, equals, TYPE);
        }

        public StringEqualsPropertyCondition(String propertyName, String equals) {
            super(propertyName, equals, TYPE);
        }

        public static StringEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringEqualsPropertyCondition(p, param);
        }
    }

    public static class StringNotEqualsPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "string!=";
        static {
            PropertyCondition.addSubClass(TYPE, StringNotEqualsPropertyCondition.class);
        }

        // for json
        protected StringNotEqualsPropertyCondition() {}

        public StringNotEqualsPropertyCondition(Property property, String equals) {
            super(property, equals, TYPE);
        }

        public StringNotEqualsPropertyCondition(String propertyName, String equals) {
            super(propertyName, equals, TYPE);
        }

        public static StringNotEqualsPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringNotEqualsPropertyCondition(p, param);
        }
    }

    public static class StringMatchPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "stringmatch";
        static {
            PropertyCondition.addSubClass(TYPE, StringMatchPropertyCondition.class);
        }

        // for json
        protected StringMatchPropertyCondition() {}

        public StringMatchPropertyCondition(Property property, String pattern) {
            super(property, pattern, TYPE);
        }

        public StringMatchPropertyCondition(String propertyName, String pattern) {
            super(propertyName, pattern, TYPE);
        }

        public static StringMatchPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringMatchPropertyCondition(p, param);
        }
    }

    public static class StringNotMatchPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "string!match";
        static {
            PropertyCondition.addSubClass(TYPE, StringNotMatchPropertyCondition.class);
        }

        // for json
        protected StringNotMatchPropertyCondition() {}

        public StringNotMatchPropertyCondition(Property property, String pattern) {
            super(property, pattern, TYPE);
        }

        public StringNotMatchPropertyCondition(String propertyName, String pattern) {
            super(propertyName, pattern, TYPE);
        }

        public static StringNotMatchPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringNotMatchPropertyCondition(p, param);
        }
    }

    public static class StringStartwithPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "stringstartwith";
        static {
            PropertyCondition.addSubClass(TYPE, StringStartwithPropertyCondition.class);
        }

        // for json
        protected StringStartwithPropertyCondition() {}

        public StringStartwithPropertyCondition(Property property, String pattern) {
            super(property, pattern, TYPE);
        }

        public StringStartwithPropertyCondition(String propertyName, String pattern) {
            super(propertyName, pattern, TYPE);
        }

        public static StringStartwithPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringStartwithPropertyCondition(p, param);
        }
    }

    public static class StringNotStartwithPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "string!startwith";
        static {
            PropertyCondition.addSubClass(TYPE, StringNotStartwithPropertyCondition.class);
        }

        // for json
        protected StringNotStartwithPropertyCondition() {}

        public StringNotStartwithPropertyCondition(Property property, String pattern) {
            super(property, pattern, TYPE);
        }

        public StringNotStartwithPropertyCondition(String propertyName, String pattern) {
            super(propertyName, pattern, TYPE);
        }

        public static StringNotStartwithPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringNotStartwithPropertyCondition(p, param);
        }
    }

    public static class StringEndwithPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "stringendwith";
        static {
            PropertyCondition.addSubClass(TYPE, StringEndwithPropertyCondition.class);
        }

        // for json
        protected StringEndwithPropertyCondition() {}

        public StringEndwithPropertyCondition(Property property, String pattern) {
            super(property, pattern, TYPE);
        }

        public StringEndwithPropertyCondition(String propertyName, String pattern) {
            super(propertyName, pattern, TYPE);
        }

        public static StringEndwithPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringEndwithPropertyCondition(p, param);
        }
    }

    public static class StringNotEndwithPropertyCondition extends StringPropertyCondition {
        public static final String TYPE = "string!endwith";
        static {
            PropertyCondition.addSubClass(TYPE, StringNotEndwithPropertyCondition.class);
        }

        // for json
        protected StringNotEndwithPropertyCondition() {}

        public StringNotEndwithPropertyCondition(Property property, String pattern) {
            super(property, pattern, TYPE);
        }

        public StringNotEndwithPropertyCondition(String propertyName, String pattern) {
            super(propertyName, pattern, TYPE);
        }

        public static StringNotEndwithPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            Property p = Property.from_json_object(map.get("srcProperty"));
            String param = (String)map.get("param");
            return new StringNotEndwithPropertyCondition(p, param);
        }
    }

}
