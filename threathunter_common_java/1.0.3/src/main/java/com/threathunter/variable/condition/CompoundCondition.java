package com.threathunter.variable.condition;

import com.threathunter.model.Property;
import com.threathunter.model.PropertyCondition;

import java.util.*;

import static com.threathunter.common.Utility.argumentNotEmpty;

/**
 * Compound condition is the base class for all the complex conditions which are based on other conditions.
 *
 * @author Wen Lu
 */
public abstract class CompoundCondition extends PropertyCondition {
    private List<PropertyCondition> conditions;

    // for json
    protected CompoundCondition() {}

    public CompoundCondition(List<PropertyCondition> conditions, String type) {
        super(type);

        argumentNotEmpty(conditions, "null conditions");

        this.conditions = conditions;
    }

    public List<PropertyCondition> getConditions() {
        return conditions;
    }

    @Override
    public Object getParam() {
        return "";
    }

    @Override
    public List<Property> getSrcProperties() {
        List<Property> result = new ArrayList<Property>();
        for(PropertyCondition c : conditions) {
            result.addAll(c.getSrcProperties());
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompoundCondition that = (CompoundCondition) o;

        if (!getType().equals(that.getType())) return false;
        return conditions.equals(that.conditions);

    }

    @Override
    public int hashCode() {
        return conditions.hashCode() * 31 + getType().hashCode();
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        List<Object> conditionObjects = new ArrayList<>();

        for(PropertyCondition subCondition : getConditions()) {
            conditionObjects.add(subCondition.to_json_object());
        }

        result.put("type", getType());
        result.put("condition", conditionObjects);
        return result;
    }

    public static class AndPropertyCondition extends CompoundCondition {
        public static final String TYPE = "and";
        static {
            PropertyCondition.addSubClass(TYPE, AndPropertyCondition.class);
        }

        // for json
        protected AndPropertyCondition() {}

        public AndPropertyCondition(List<PropertyCondition> conditions) {
            super(conditions, TYPE);
        }

        public static AndPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            List<Object> conditionObjects = (List<Object>)map.get("condition");
            List<PropertyCondition> conditions = new ArrayList<>();
            for(Object o : conditionObjects) {
                conditions.add(PropertyCondition.from_json_object(o));
            }
            if (conditions.isEmpty()) {
                return null;
            }

            return new AndPropertyCondition(conditions);
        }
    }

    public static class OrPropertyCondition extends CompoundCondition {
        public static final String TYPE = "or";
        static {
            PropertyCondition.addSubClass(TYPE, OrPropertyCondition.class);
        }

        // for json
        protected OrPropertyCondition() {}

        public OrPropertyCondition(List<PropertyCondition> conditions) {
            super(conditions, TYPE);
        }

        public static OrPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            List<Object> conditionObjects = (List<Object>)map.get("condition");
            List<PropertyCondition> conditions = new ArrayList<>();
            for(Object o : conditionObjects) {
                conditions.add(PropertyCondition.from_json_object(o));
            }
            if (conditions.isEmpty()) {
                return null;
            }

            return new OrPropertyCondition(conditions);
        }
    }

    public static class NotPropertyCondition extends CompoundCondition {
        public static final String TYPE = "not";
        static {
            PropertyCondition.addSubClass(TYPE, NotPropertyCondition.class);
        }

        // for json
        protected NotPropertyCondition() {}

        public NotPropertyCondition(List<PropertyCondition> conditions) {
            super(conditions, TYPE);
            if (conditions.size() != 1) {
                throw new IllegalStateException("not condition can only have 1 member");
            }
        }

        public NotPropertyCondition(PropertyCondition condition) {
            this(Arrays.asList(condition));
        }

        public static NotPropertyCondition from_json_object(Object obj) {
            Map<Object, Object> map = (Map<Object, Object>)obj;
            List<Object> conditionObjects = (List<Object>)map.get("condition");
            List<PropertyCondition> conditions = new ArrayList<>();
            if (conditionObjects != null) {
                for(Object o : conditionObjects) {
                    conditions.add(PropertyCondition.from_json_object(o));
                }
            }
            if (conditions.isEmpty()) {
                return null;
            }

            return new NotPropertyCondition(conditions);
        }
    }
}
