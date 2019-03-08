package com.threathunter.variable.reduction;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyReduction;

import java.util.HashMap;
import java.util.Map;

/**
 * Generate a new property from a {@code StringProperty}.
 *
 * @author Wen Lu
 */
public abstract class StringPropertyReduction extends PropertyReduction {

    // for json
    protected StringPropertyReduction() {}

    public StringPropertyReduction(Property srcProperty, Property destProperty, String type, String method) {
        super(srcProperty, destProperty, type, method);
        if (srcProperty.getType() != NamedType.STRING) {
            throw new IllegalArgumentException("the source property is not string");
        }
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("srcProperty", getSrcProperties().get(0).to_json_object());
        result.put("destProperty", getDestProperty().to_json_object());
        return result;
    }

    public static class StringCountPropertyReduction extends StringPropertyReduction {
        public static final String TYPE = "stringcount";
        static {
            PropertyReduction.addSubClass(TYPE, StringCountPropertyReduction.class);
        }

        // for json
        protected StringCountPropertyReduction() {}

        public StringCountPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE, "count");
        }

        public StringCountPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildStringProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE, "count");
        }

        public StringCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                            Identifier destID, String destPropertyName) {
            super(Property.buildStringProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE, "count");
        }

        public static StringCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new StringCountPropertyReduction(src, dest);
        }
    }

    public static class StringGroupCountPropertyReduction extends StringPropertyReduction {
        public static final String TYPE = "stringgroup_count";
        private final Property groupProperty;
        static {
            PropertyReduction.addSubClass(TYPE, StringPropertyReduction.StringGroupCountPropertyReduction.class);
        }

        public Property getGroupProperty() {
            return this.groupProperty;
        }

        public int getLimit() {
            return 20;
        }

        // for json
        protected StringGroupCountPropertyReduction() {
            this.groupProperty = null;
        }

        public StringGroupCountPropertyReduction(Property srcProperty, Property destProperty, Property groupProperty) {
            super(srcProperty, destProperty, TYPE, "group_count");
            this.groupProperty = groupProperty;
        }

        public StringGroupCountPropertyReduction(String srcPropertyName, String destPropertyName, String groupPropertyName) {
            super(Property.buildStringProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE, "group_count");
            this.groupProperty = Property.buildStringProperty(groupPropertyName);
        }

        public StringGroupCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                               Identifier destID, String destPropertyName, String groupPropertyName) {
            super(Property.buildStringProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName),
                    TYPE, "group_count");
            this.groupProperty = Property.buildStringProperty(srcID, groupPropertyName);
        }

        public static StringPropertyReduction.StringGroupCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.MAP);
            dest.setSubType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            Property groupProperty = Property.buildStringProperty(src.getIdentifier(), (String) map.get("param"));
            return new StringPropertyReduction.StringGroupCountPropertyReduction(src, dest, groupProperty);
        }
    }

    public static class StringDistinctCountPropertyReduction extends StringPropertyReduction {
        public static final String TYPE = "stringdistinct_count";
        static {
            PropertyReduction.addSubClass(TYPE, StringDistinctCountPropertyReduction.class);
        }

        // for json
        protected StringDistinctCountPropertyReduction() {}

        public StringDistinctCountPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE, "distinct_count");
        }

        public StringDistinctCountPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildStringProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE, "distinct_count");
        }

        public StringDistinctCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                                    Identifier destID, String destPropertyName) {
            super(Property.buildStringProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName),
                    TYPE, "distinct_count");
        }

        public static StringDistinctCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new StringDistinctCountPropertyReduction(src, dest);
        }
    }
    public static class StringListDistinctCountPropertyReduction extends StringPropertyReduction {
        public static final String TYPE = "stringlistdistinct_count";
        static {
            PropertyReduction.addSubClass(TYPE, StringDistinctCountPropertyReduction.class);
        }

        // for json
        protected StringListDistinctCountPropertyReduction() {}

        public StringListDistinctCountPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE, "distinct_count");
        }

        public StringListDistinctCountPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildStringProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE, "distinct_count");
        }

        public StringListDistinctCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                                        Identifier destID, String destPropertyName) {
            super(Property.buildStringProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE, "distinct_count");
        }

        public static StringListDistinctCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new StringListDistinctCountPropertyReduction(src, dest);
        }
    }
    public static class StringListCountPropertyReduction extends StringPropertyReduction {
        public static final String TYPE = "stringlistcount";
        static {
            PropertyReduction.addSubClass(TYPE, StringListCountPropertyReduction.class);
        }

        // for json
        protected StringListCountPropertyReduction() {}

        public StringListCountPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE, "count");
        }

        public StringListCountPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildStringProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE, "count");
        }

        public StringListCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                                Identifier destID, String destPropertyName) {
            super(Property.buildStringProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE, "count");
        }

        public static StringListCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new StringListCountPropertyReduction(src, dest);
        }
    }
    public static class StringLastPropertyReduction extends StringPropertyReduction {
        public static final String TYPE = "stringlast";
        static {
            PropertyReduction.addSubClass(TYPE, StringLastPropertyReduction.class);
        }

        // for json
        protected StringLastPropertyReduction() {}

        public StringLastPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE, "last");
        }

        public StringLastPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildStringProperty(srcPropertyName), Property.buildStringProperty(destPropertyName), TYPE, "last");
        }

        public StringLastPropertyReduction(Identifier srcID, String srcPropertyName,
                                             Identifier destID, String destPropertyName) {
            super(Property.buildStringProperty(srcID, srcPropertyName), Property.buildStringProperty(destID, destPropertyName), TYPE, "last");
        }

        public static StringLastPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.STRING);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new StringLastPropertyReduction(src, dest);
        }
    }
}
