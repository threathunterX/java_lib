package com.threathunter.variable.meta;

import com.threathunter.model.Property;
import com.threathunter.model.VariableMetaRegistry;
import com.threathunter.variable.condition.ConditionParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class SequenceVariableMeta extends BaseVariableMeta {
    public static final String TYPE = "sequence";
    static {
        addSubClass(TYPE, SequenceVariableMeta.class);
    }

    private String operation;
    private Property targetProperty;

    protected SequenceVariableMeta() {

    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Property getTargetProperty() {
        return targetProperty;
    }

    public void setTargetProperty(Property targetProperty) {
        this.targetProperty = targetProperty;
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = (Map<Object, Object>) super.to_json_object();
        result.put("filter_field", ConditionParser.toJsonObject(this.condition));
        Map<String, Object> function = new HashMap<>();
        function.put("method", this.operation);
        function.put("object", this.targetProperty.getName());

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SequenceVariableMeta that = (SequenceVariableMeta) o;

        if (getTtl() != that.getTtl()) return false;
        if (!getGroupKeys().equals(that.getGroupKeys())) return false;
        if (!condition.equals(that.getPropertyCondition())) return false;
        return targetProperty.equals(that.getTargetProperty()) && operation.equals(that.getOperation());
    }

    @Override
    protected void initialFunction(Map<String, Object> functionJson) {
        this.setOperation((String) functionJson.get("method"));
        this.setTargetProperty(VariableMetaRegistry.getInstance().getVariableMeta(this.getSrcVariableMetasID().get(0)).findPropertyByName((String) functionJson.get("object")));
    }

    @Override
    protected List<Property> parseProperties() {
        List<Property> propertyList = new ArrayList<>();
        if (this.groupKeys != null && groupKeys.size() > 0) {
            this.getGroupKeys().forEach(property -> propertyList.add(new Property(this.getId(), property.getName(), property.getType(), property.getSubType())));
        }
        return propertyList;
    }

    public static SequenceVariableMeta from_json_object(Object obj) {
        SequenceVariableMeta result = new SequenceVariableMeta();
        result = from_json_object(obj, result);

        return result;
    }
}
