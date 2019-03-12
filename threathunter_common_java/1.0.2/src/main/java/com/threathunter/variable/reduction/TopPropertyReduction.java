package com.threathunter.variable.reduction;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyReduction;

import java.util.HashMap;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public abstract class TopPropertyReduction extends PropertyReduction {

    protected final int number;

    protected TopPropertyReduction() {
        number = 100;
    }

    public TopPropertyReduction(Property srcProperty, Property destProperty, String type, String number) {
        super(srcProperty, destProperty, type);
        this.number = Integer.valueOf(number);
    }

    public int getNumber() {
        return number;
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("scrProperty", getSrcProperties().get(0).to_json_object());
        result.put("destProperty", getDestProperty().to_json_object());
        return result;
    }

    public static class DoubleTopNPropertyReduction extends TopPropertyReduction {
        public static final String TYPE = "doubletop";

        static {
            PropertyReduction.addSubClass(TYPE, DoubleTopNPropertyReduction.class);
        }

        public DoubleTopNPropertyReduction(Property srcProperty, Property destProperty, String number) {
            super(srcProperty, destProperty, TYPE, number);
        }

        public static DoubleTopNPropertyReduction from_json_object(Object obj) {
            Map<String, Object> map = (Map<String, Object>) obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));

            return new DoubleTopNPropertyReduction(src, dest, (String) map.getOrDefault("param", "100"));
        }

        @Override
        public Object to_json_object() {
            return null;
        }
    }
    public static class LongTopNPropertyReduction extends TopPropertyReduction {
        public static final String TYPE = "longtop";

        static {
            PropertyReduction.addSubClass(TYPE, LongTopNPropertyReduction.class);
        }

        public LongTopNPropertyReduction(Property srcProperty, Property destProperty, String number) {
            super(srcProperty, destProperty, TYPE, number);
        }

        public static LongTopNPropertyReduction from_json_object(Object obj) {
            Map<String, Object> map = (Map<String, Object>) obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));

            return new LongTopNPropertyReduction(src, dest, (String) map.getOrDefault("param", "100"));
        }

        @Override
        public Object to_json_object() {
            return null;
        }
    }

    public static class DoubleKeyTopNPropertyReduction extends TopPropertyReduction {
        public static final String TYPE = "doublemaptop";

        static {
            PropertyReduction.addSubClass(TYPE, DoubleKeyTopNPropertyReduction.class);
        }

        public DoubleKeyTopNPropertyReduction(Property srcProperty, Property destProperty, String number) {
            super(srcProperty, destProperty, TYPE, number);
        }

        public static DoubleKeyTopNPropertyReduction from_json_object(Object obj) {
            Map<String, Object> map = (Map<String, Object>) obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));

            return new DoubleKeyTopNPropertyReduction(src, dest, (String) map.getOrDefault("param", "100"));
        }

    }

    public static class LongKeyTopNPropertyReduction extends TopPropertyReduction {
        public static final String TYPE = "longmaptop";

        static {
            PropertyReduction.addSubClass(TYPE, LongKeyTopNPropertyReduction.class);
        }

        public LongKeyTopNPropertyReduction(Property srcProperty, Property destProperty, String number) {
            super(srcProperty, destProperty, TYPE, number);
        }

        public static LongKeyTopNPropertyReduction from_json_object(Object obj) {
            Map<String, Object> map = (Map<String, Object>) obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));

            return new LongKeyTopNPropertyReduction(src, dest, (String) map.getOrDefault("param", "100"));
        }

    }
}
