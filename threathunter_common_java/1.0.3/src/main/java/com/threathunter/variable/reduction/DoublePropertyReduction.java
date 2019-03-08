package com.threathunter.variable.reduction;


import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyReduction;

import java.util.HashMap;
import java.util.Map;

/**
 * Generate a new property from a {@code DoubleProperty}.
 *
 * @author Wen Lu
 */
public abstract class DoublePropertyReduction extends PropertyReduction {

    // for json
    protected DoublePropertyReduction() {}

    public DoublePropertyReduction(Property srcProperty, Property destProperty, String type) {
        super(srcProperty, destProperty, type);
        if (srcProperty.getType() != NamedType.DOUBLE) {
            throw new IllegalArgumentException("the source property is not double");
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

    public static class DoubleMaxPropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doublemax";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleMaxPropertyReduction.class);
        }

        // for json
        protected DoubleMaxPropertyReduction() {}

        public DoubleMaxPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleMaxPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public DoubleMaxPropertyReduction(Identifier srcID, String srcPropertyName,
                                          Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleMaxPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleMaxPropertyReduction(src, dest);
        }

    }

    public static class DoubleMinPropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doublemin";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleMinPropertyReduction.class);
        }

        // for json
        protected DoubleMinPropertyReduction() {}

        public DoubleMinPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleMinPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public DoubleMinPropertyReduction(Identifier srcID, String srcPropertyName,
                                          Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleMinPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleMinPropertyReduction(src, dest);
        }
    }

    public static class DoubleSumPropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doublesum";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleSumPropertyReduction.class);
        }

        // for json
        protected DoubleSumPropertyReduction() {}

        public DoubleSumPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleSumPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public DoubleSumPropertyReduction(Identifier srcID, String srcPropertyName,
                                          Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleSumPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleSumPropertyReduction(src, dest);
        }
    }

    public static class DoubleCountPropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doublecount";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleCountPropertyReduction.class);
        }

        // for json
        protected DoubleCountPropertyReduction() {}

        public DoubleCountPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleCountPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public DoubleCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                            Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleCountPropertyReduction(src, dest);
        }
    }

    public static class DoubleGroupCountPropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doublegroup_count";
        private final Property groupProperty;
        static {
            PropertyReduction.addSubClass(TYPE, DoubleGroupCountPropertyReduction.class);
        }

        public Property getGroupProperty() {
            return this.groupProperty;
        }

        public int getLimit() {
            return 20;
        }

        // for json
        protected DoubleGroupCountPropertyReduction() {
            this.groupProperty = null;
        }

        public DoubleGroupCountPropertyReduction(Property srcProperty, Property destProperty, Property groupProperty) {
            super(srcProperty, destProperty, TYPE);
            this.groupProperty = groupProperty;
        }

        public DoubleGroupCountPropertyReduction(String srcPropertyName, String destPropertyName, String groupPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
            this.groupProperty = Property.buildStringProperty(groupPropertyName);
        }

        public DoubleGroupCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                             Identifier destID, String destPropertyName, String groupPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
            this.groupProperty = Property.buildStringProperty(srcID, groupPropertyName);
        }

        public static DoubleGroupCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.MAP);
            dest.setSubType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            Property groupProperty = Property.buildStringProperty(src.getIdentifier(), (String) map.get("param"));
            return new DoubleGroupCountPropertyReduction(src, dest, groupProperty);
        }
    }

    public static class DoubleDistinctCountPropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doubledistinctcount";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleDistinctCountPropertyReduction.class);
        }

        // for json
        protected DoubleDistinctCountPropertyReduction() {}

        public DoubleDistinctCountPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleDistinctCountPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public DoubleDistinctCountPropertyReduction(Identifier srcID, String srcPropertyName,
                                                    Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleDistinctCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleDistinctCountPropertyReduction(src, dest);
        }
    }

    public static class DoubleAvgPropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doubleavg";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleAvgPropertyReduction.class);
        }

        // for json
        protected DoubleAvgPropertyReduction() {}

        public DoubleAvgPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleAvgPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public DoubleAvgPropertyReduction(Identifier srcID, String srcPropertyName,
                                          Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleAvgPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleAvgPropertyReduction(src, dest);
        }
    }

    public static class DoubleFirstPropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doublefirst";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleFirstPropertyReduction.class);
        }

        // for json
        protected DoubleFirstPropertyReduction() {}

        public DoubleFirstPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleFirstPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public DoubleFirstPropertyReduction(Identifier srcID, String srcPropertyName,
                                            Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleFirstPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleFirstPropertyReduction(src, dest);
        }
    }

    public static class DoubleLastPropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doublelast";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleLastPropertyReduction.class);
        }

        // for json
        protected DoubleLastPropertyReduction() {}

        public DoubleLastPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleLastPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public DoubleLastPropertyReduction(Identifier srcID, String srcPropertyName,
                                           Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleLastPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleLastPropertyReduction(src, dest);
        }
    }

    public static class DoubleStddevPropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doublestddev";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleStddevPropertyReduction.class);
        }

        // for json
        protected DoubleStddevPropertyReduction() {}

        public DoubleStddevPropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleStddevPropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public DoubleStddevPropertyReduction(Identifier srcID, String srcPropertyName,
                                             Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleStddevPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleStddevPropertyReduction(src, dest);
        }
    }

    public static class DoubleRangePropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doublerange";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleRangePropertyReduction.class);
        }

        // for json
        protected DoubleRangePropertyReduction() {}

        public DoubleRangePropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleRangePropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public DoubleRangePropertyReduction(Identifier srcID, String srcPropertyName,
                                            Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleRangePropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleRangePropertyReduction(src, dest);
        }
    }

    public static class DoubleAmplitudePropertyReduction extends DoublePropertyReduction {
        public static final String TYPE = "doubleamplitude";
        static {
            PropertyReduction.addSubClass(TYPE, DoubleAmplitudePropertyReduction.class);
        }

        // for json
        protected DoubleAmplitudePropertyReduction() {}

        public DoubleAmplitudePropertyReduction(Property srcProperty, Property destProperty) {
            super(srcProperty, destProperty, TYPE);
        }

        public DoubleAmplitudePropertyReduction(String srcPropertyName, String destPropertyName) {
            super(Property.buildDoubleProperty(srcPropertyName), Property.buildDoubleProperty(destPropertyName), TYPE);
        }

        public DoubleAmplitudePropertyReduction(Identifier srcID, String srcPropertyName,
                                                Identifier destID, String destPropertyName) {
            super(Property.buildDoubleProperty(srcID, srcPropertyName), Property.buildDoubleProperty(destID, destPropertyName), TYPE);
        }

        public static DoubleAmplitudePropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            Property src = Property.from_json_object(map.get("srcProperty"));
            return new DoubleAmplitudePropertyReduction(src, dest);
        }
    }
}
