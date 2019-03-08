package com.threathunter.variable.reduction;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyReduction;

import java.util.HashMap;
import java.util.Map;

/**
 * Generate a new property from a {@code LongProperty}.
 *
 * @author Wen Lu
 */
public abstract class LongPropertyReduction extends PropertyReduction {

    // for json
    protected LongPropertyReduction() {}

    public LongPropertyReduction(Property srcProperty, Property destProperty, String type) {
        super(srcProperty, destProperty, type);
        if (srcProperty.getType() != NamedType.LONG)
            throw new IllegalArgumentException("the source type is not long");
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("srcProperty", getSrcProperties().get(0).to_json_object());
        result.put("destProperty", getDestProperty().to_json_object());
        return result;
    }

    public static class LongMaxPropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longmax";
        static {
            PropertyReduction.addSubClass(TYPE, LongMaxPropertyReduction.class);
        }

        // for json
        protected LongMaxPropertyReduction() {}

        public LongMaxPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongMaxPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public LongMaxPropertyReduction(Identifier srcID, String srcPropertyName,
                                        Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static LongMaxPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongMaxPropertyReduction(src, dest);
        }

    }

    public static class LongMinPropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longmin";
        static {
            PropertyReduction.addSubClass(TYPE, LongMinPropertyReduction.class);
        }

        // for json
        protected LongMinPropertyReduction() {}

        public LongMinPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongMinPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public LongMinPropertyReduction(Identifier srcID, String srcPropertyName,
                                        Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static LongMinPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongMinPropertyReduction(src, dest);
        }
    }

    public static class LongSumPropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longsum";
        static {
            PropertyReduction.addSubClass(TYPE, LongSumPropertyReduction.class);
        }

        // for json
        protected LongSumPropertyReduction() {}

        public LongSumPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongSumPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public LongSumPropertyReduction(Identifier srcID, String srcPropertyName,
                                        Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static LongSumPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongSumPropertyReduction(src, dest);
        }
    }

    public static class LongCountPropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longcount";
        static {
            PropertyReduction.addSubClass(TYPE, LongCountPropertyReduction.class);
        }

        // for json
        protected LongCountPropertyReduction() {}

        public LongCountPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongCountPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public LongCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                          Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static LongCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongCountPropertyReduction(src, dest);
        }
    }

    public static class LongGroupCountPropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longgroup_count";
        static {
            PropertyReduction.addSubClass(TYPE, LongCountPropertyReduction.class);
        }

        private int limit = 20;
        // for json
        protected LongGroupCountPropertyReduction() {}

        public LongGroupCountPropertyReduction(Property srcProperty, Property destProperty, String limit) {
            super(srcProperty, destProperty, TYPE);
            if (limit != null && !limit.isEmpty()) {
                this.limit = Integer.parseInt(limit);
            }
        }

        public LongGroupCountPropertyReduction(String srcPropertyName, String destPropertyName) {
            // TODO destProperty type should be map
            super(Property.buildLongProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public LongGroupCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                               Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public int getLimit() {
            return limit;
        }

        public static LongGroupCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.MAP);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongGroupCountPropertyReduction(src, dest, (String) map.getOrDefault("param", "20"));
        }
    }

    public static class LongDistinctCountPropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longdistinct_count";
        static {
            PropertyReduction.addSubClass(TYPE, LongDistinctCountPropertyReduction.class);
        }

        // for json
        protected LongDistinctCountPropertyReduction() {}

        public LongDistinctCountPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongDistinctCountPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public LongDistinctCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                                  Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static LongDistinctCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongDistinctCountPropertyReduction(src, dest);
        }
    }

    public static class LongAvgPropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longavg";
        static {
            PropertyReduction.addSubClass(TYPE, LongAvgPropertyReduction.class);
        }

        // for json
        protected LongAvgPropertyReduction() {}

        public LongAvgPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongAvgPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public LongAvgPropertyReduction(Identifier srcID, String srcPropertyName,
                                        Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static LongAvgPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongAvgPropertyReduction(src, dest);
        }
    }

    public static class LongFirstPropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longfirst";
        static {
            PropertyReduction.addSubClass(TYPE, LongFirstPropertyReduction.class);
        }

        // for json
        protected LongFirstPropertyReduction() {}

        public LongFirstPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongFirstPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public LongFirstPropertyReduction(Identifier srcID, String srcPropertyName,
                                          Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static LongFirstPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongFirstPropertyReduction(src, dest);
        }
    }

    public static class LongLastPropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longlast";
        static {
            PropertyReduction.addSubClass(TYPE, LongLastPropertyReduction.class);
        }

        // for json
        protected LongLastPropertyReduction() {}

        public LongLastPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongLastPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public LongLastPropertyReduction(Identifier srcID, String srcPropertyName,
                                         Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static LongLastPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongLastPropertyReduction(src, dest);
        }
    }

    public static class LongStddevPropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longstddev";
        static {
            PropertyReduction.addSubClass(TYPE, LongStddevPropertyReduction.class);
        }

        // for json
        protected LongStddevPropertyReduction() {}

        public LongStddevPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongStddevPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public LongStddevPropertyReduction(Identifier srcID, String srcPropertyName,
                                           Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static LongStddevPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongStddevPropertyReduction(src, dest);
        }
    }

    public static class LongRangePropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longrange";
        static {
            PropertyReduction.addSubClass(TYPE, LongRangePropertyReduction.class);
        }

        // for json
        protected LongRangePropertyReduction() {}

        public LongRangePropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongRangePropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public LongRangePropertyReduction(Identifier srcID, String srcPropertyName,
                                          Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static LongRangePropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongRangePropertyReduction(src, dest);
        }
    }

    public static class LongAmplitudePropertyReduction extends LongPropertyReduction {
        public static final String TYPE = "longamplitude";
        static {
            PropertyReduction.addSubClass(TYPE, LongAmplitudePropertyReduction.class);
        }

        // for json
        protected LongAmplitudePropertyReduction() {}

        public LongAmplitudePropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public LongAmplitudePropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildLongProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public LongAmplitudePropertyReduction(Identifier srcID, String srcPropertyName,
                                              Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static LongAmplitudePropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new LongAmplitudePropertyReduction(src, dest);
        }
    }
}
