package com.threathunter.variable.reduction;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyReduction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daisy on 17-10-31
 */
public abstract class TopPropertyReduction extends PropertyReduction {

    protected final String topKey;

    protected TopPropertyReduction() {
        topKey = "";
    }

    public TopPropertyReduction(Property srcProperty, Property destProperty, String type, String topKey) {
        super(srcProperty, destProperty, type, "top");
        this.topKey = topKey;
    }

    public String getTopKey() {
        return topKey;
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
        // TODO should be doubletop, because the bug of vargen, source value type should be double instead of map
        // see did__visit_dynamic_distinct_count_ip_top100__1h__slot
        public static final String TYPE = "doubledoubletop";

        static {
            PropertyReduction.addSubClass(TYPE, DoubleTopNPropertyReduction.class);
        }

        public DoubleTopNPropertyReduction(Property srcProperty, Property destProperty, String topKey) {
            super(srcProperty, destProperty, TYPE, topKey);
        }

        public static DoubleTopNPropertyReduction from_json_object(Object obj) {
            Map<String, Object> map = (Map<String, Object>) obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            String topKey = (String) map.get("param");

            return new DoubleTopNPropertyReduction(src, dest, topKey);
        }

        @Override
        public Object to_json_object() {
            return null;
        }
    }
    public static class LongTopNPropertyReduction extends TopPropertyReduction {
        // TODO the same with doubledoubletop
        public static final String TYPE = "longlongtop";

        static {
            PropertyReduction.addSubClass(TYPE, LongTopNPropertyReduction.class);
        }

        public LongTopNPropertyReduction(Property srcProperty, Property destProperty, String topKey) {
            super(srcProperty, destProperty, TYPE, topKey);
        }

        public static LongTopNPropertyReduction from_json_object(Object obj) {
            Map<String, Object> map = (Map<String, Object>) obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.LONG);
            Property src = Property.from_json_object(map.get("srcProperty"));
            String topKey = (String) map.get("param");

            return new LongTopNPropertyReduction(src, dest, topKey);
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

        public DoubleKeyTopNPropertyReduction(Property srcProperty, Property destProperty, String topKey) {
            super(srcProperty, destProperty, TYPE, topKey);
        }

        public static DoubleKeyTopNPropertyReduction from_json_object(Object obj) {
            Map<String, Object> map = (Map<String, Object>) obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            String topKey = (String) map.get("param");

            return new DoubleKeyTopNPropertyReduction(src, dest, topKey);
        }

    }

    public static class LongKeyTopNPropertyReduction extends TopPropertyReduction {
        public static final String TYPE = "longmaptop";

        static {
            PropertyReduction.addSubClass(TYPE, LongKeyTopNPropertyReduction.class);
        }

        public LongKeyTopNPropertyReduction(Property srcProperty, Property destProperty, String topKey) {
            super(srcProperty, destProperty, TYPE, topKey);
        }

        public static LongKeyTopNPropertyReduction from_json_object(Object obj) {
            Map<String, Object> map = (Map<String, Object>) obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            dest.setType(NamedType.DOUBLE);
            Property src = Property.from_json_object(map.get("srcProperty"));
            String topKey = (String) map.get("param");

            return new LongKeyTopNPropertyReduction(src, dest, topKey);
        }

    }
}
