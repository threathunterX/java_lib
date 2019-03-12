package com.threathunter.variable.reduction;

import com.threathunter.common.Identifier;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyReduction;

import java.util.HashMap;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public abstract class SubPropertyReduction extends PropertyReduction {
    protected String split;
    protected String joinPropertyName;

    protected SubPropertyReduction() {}

    public SubPropertyReduction(Property srcProperty, Property destProperty, String type, String split, String joinPropertyName) {
        super(srcProperty, destProperty, type);
        this.split = split;
        this.joinPropertyName = joinPropertyName;
    }

    public String getSplit() {
        return this.split;
    }

    public String getJoinPropertyName() {
        return this.joinPropertyName;
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("srcProperty", getSrcProperties().get(0).to_json_object());
        result.put("destProperty", getDestProperty().to_json_object());
        result.put("split", this.split);
        result.put("join_property", this.joinPropertyName);

        return result;
    }

    public static class SubCountPropertyReduction extends SubPropertyReduction {
        public static final String TYPE = "subcount";
        static {
            PropertyReduction.addSubClass(TYPE, SubCountPropertyReduction.class);
        }

        protected SubCountPropertyReduction() {}

        public SubCountPropertyReduction(Property srcProperty, Property destProperty, String split, String joinPropertyName) {
            super(srcProperty, destProperty, TYPE, split, joinPropertyName);
        }

        public SubCountPropertyReduction(String srcPropertyName, String destPropertyName, String split, String joinPropertyName) {
            super(Property.buildStringProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE, split, joinPropertyName);
        }

        public SubCountPropertyReduction(Identifier srcID, String srcPropertyName, Identifier destID, String destPropertyName, String split, String joinPropertyName) {
            super(Property.buildStringProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE, split, joinPropertyName);
        }

        public static SubCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>) obj;
            Property dest = Property.from_json_object(map.get("destProperty"));
            Property src = Property.from_json_object(map.get("srcProperty"));
            String split = (String) map.get("split");
            String joinPropertyName = (String) map.get("join_property");
            return new SubCountPropertyReduction(src, dest, split, joinPropertyName);
        }
    }

    public static class SubDistinctCountPropertyReduction extends SubPropertyReduction {
        public static final String TYPE = "subdistinctcount";
        static {
            PropertyReduction.addSubClass(TYPE, SubCountPropertyReduction.class);
        }

        protected SubDistinctCountPropertyReduction() {}

        public SubDistinctCountPropertyReduction(Property srcProperty, Property destProperty, String split, String joinPropertyName) {
            super(srcProperty, destProperty, TYPE, split, joinPropertyName);
        }

        public SubDistinctCountPropertyReduction(String srcPropertyName, String destPropertyName, String split, String joinPropertyName) {
            super(Property.buildStringProperty(srcPropertyName), Property.buildLongProperty(destPropertyName), TYPE, split, joinPropertyName);
        }

        public SubDistinctCountPropertyReduction(Identifier srcID, String srcPropertyName, Identifier destID, String destPropertyName, String split, String joinPropertyName) {
            super(Property.buildStringProperty(srcID, srcPropertyName), Property.buildLongProperty(destID, destPropertyName), TYPE, split, joinPropertyName);
        }

        public static SubDistinctCountPropertyReduction from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>) obj;
            Property dest = Property.from_json_object(map.get("destProperty"));
            Property src = Property.from_json_object(map.get("srcProperty"));
            String split = (String) map.get("split");
            String joinPropertyName = (String) map.get("join_property");
            return new SubDistinctCountPropertyReduction(src, dest, split, joinPropertyName);
        }
    }
}
