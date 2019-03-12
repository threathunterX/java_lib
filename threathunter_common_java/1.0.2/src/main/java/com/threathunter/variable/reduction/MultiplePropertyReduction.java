package com.threathunter.variable.reduction;

import com.threathunter.common.Identifier;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyReduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.threathunter.variable.json.JsonUtil.property_list_from_json_object;
import static com.threathunter.variable.json.JsonUtil.property_list_to_json_object;

/**
 * created by www.threathunter.cn
 */
public abstract class MultiplePropertyReduction extends PropertyReduction {
    // for json
    protected MultiplePropertyReduction() {}

    public MultiplePropertyReduction(List<Property> srcProperties, Property destProperty, String type) {
        super(srcProperties, destProperty, type);
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("destProperty", getDestProperty().to_json_object());
        result.put("srcProperties", property_list_to_json_object(getSrcProperties()));
        return result;
    }

    public static class MultipleCountPropertyReduction extends MultiplePropertyReduction {
        public static final String TYPE = "multiplecount";
        static {
            PropertyReduction.addSubClass(TYPE, MultipleCountPropertyReduction.class);
        }

        // for json
        protected MultipleCountPropertyReduction() {}

        public MultipleCountPropertyReduction(List<Property> srcProperties, Property destProperty) {
            super(srcProperties, destProperty, TYPE);
        }

        public MultipleCountPropertyReduction(String destPropertyName) {
            super(new ArrayList<Property>(), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public MultipleCountPropertyReduction(Identifier destID, String destPropertyName) {
            super(new ArrayList<Property>(), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static MultipleCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            Object srcPropertiesObj = map.get("srcProperties");
            if (srcPropertiesObj == null) {
                srcPropertiesObj = new ArrayList<Property>();
            }
            List<Property> srcProperties = property_list_from_json_object((List<Object>) srcPropertiesObj);
            return new MultipleCountPropertyReduction(srcProperties, dest);
        }
    }

    public static class MultipleDistinctCountPropertyReduction extends MultiplePropertyReduction {
        public static final String TYPE = "multipledistinctcount";
        static {
            PropertyReduction.addSubClass(TYPE, MultipleDistinctCountPropertyReduction.class);
        }

        // for json
        protected MultipleDistinctCountPropertyReduction() {}

        public MultipleDistinctCountPropertyReduction(List<Property> srcProperties, Property destProperty) {
            super(srcProperties, destProperty, TYPE);
        }

        public MultipleDistinctCountPropertyReduction(String destPropertyName) {
            super(new ArrayList<Property>(), Property.buildLongProperty(destPropertyName), TYPE);
        }

        public MultipleDistinctCountPropertyReduction(Identifier destID, String destPropertyName) {
            super(new ArrayList<Property>(), Property.buildLongProperty(destID, destPropertyName), TYPE);
        }

        public static MultipleDistinctCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;

            Property dest = Property.from_json_object(map.get("destProperty"));
            Object srcPropertiesObj = map.get("srcProperties");
            if (srcPropertiesObj == null) {
                srcPropertiesObj = new ArrayList<Property>();
            }
            List<Property> srcProperties = property_list_from_json_object((List<Object>) srcPropertiesObj);
            return new MultipleDistinctCountPropertyReduction(srcProperties, dest);
        }
    }
}
