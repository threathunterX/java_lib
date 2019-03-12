package com.threathunter.variable.reduction;

import com.threathunter.common.Identifier;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyReduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public abstract class WildcardPropertyReduction extends PropertyReduction {
    // for json
    protected WildcardPropertyReduction() {}

    public WildcardPropertyReduction(Property destProperty, String type, String method) {
        super(new ArrayList<Property>(), destProperty, type, method);
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("destProperty", getDestProperty().to_json_object());
        return result;
    }

    public static class WildcardCountPropertyReduction extends WildcardPropertyReduction {
        public static final String TYPE = "wildcardcount";
        static {
            PropertyReduction.addSubClass(TYPE, WildcardCountPropertyReduction.class);
        }

        // for json
        protected WildcardCountPropertyReduction() {}

        public WildcardCountPropertyReduction(Property destProperty) {
            super(destProperty, TYPE, "count");
        }

        public WildcardCountPropertyReduction(String destPropertyName) {
            super(Property.buildLongProperty(destPropertyName), TYPE, "count");
        }

        public WildcardCountPropertyReduction(Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(destID, destPropertyName), TYPE, "count");
        }

        public static WildcardCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            return new WildcardCountPropertyReduction(dest);
        }
    }

    public static class WildcardDistinctCountPropertyReduction extends WildcardPropertyReduction {
        public static final String TYPE = "wildcarddistinct_count";
        static {
            PropertyReduction.addSubClass(TYPE, WildcardDistinctCountPropertyReduction.class);
        }

        // for json
        protected WildcardDistinctCountPropertyReduction() {}

        public WildcardDistinctCountPropertyReduction(Property destProperty) {
            super(destProperty, TYPE, "distinct_count");
        }

        public WildcardDistinctCountPropertyReduction(String destPropertyName) {
            super(Property.buildLongProperty(destPropertyName), TYPE, "distinct_count");
        }

        public WildcardDistinctCountPropertyReduction(Identifier destID, String destPropertyName) {
            super(Property.buildLongProperty(destID, destPropertyName), TYPE, "distinct_count");
        }

        public static WildcardDistinctCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            return new WildcardDistinctCountPropertyReduction(dest);
        }
    }
}
