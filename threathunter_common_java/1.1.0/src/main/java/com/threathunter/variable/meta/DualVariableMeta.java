package com.threathunter.variable.meta;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * created by www.threathunter.cn
 */
public class DualVariableMeta extends BaseVariableMeta {
    public static final String TYPE = "dual";

    static {
        addSubClass(TYPE, DualVariableMeta.class);
    }

    private Identifier firstId;
    private Identifier secondId;
    private boolean firstMayNull = false;
    private boolean secondMayNull = false;
    private String operation;
    private Property firstProperty;
    private Property secondProperty;

    public Identifier getFirstId() {
        return firstId;
    }

    public void setFirstId(Identifier firstId) {
        this.firstId = firstId;
    }

    public Identifier getSecondId() {
        return secondId;
    }

    public void setSecondId(Identifier secondId) {
        this.secondId = secondId;
    }

    public boolean isFirstMayNull() {
        return firstMayNull;
    }

    public void setFirstMayNull(boolean firstMayNull) {
        this.firstMayNull = firstMayNull;
    }

    public boolean isSecondMayNull() {
        return secondMayNull;
    }

    public void setSecondMayNull(boolean secondMayNull) {
        this.secondMayNull = secondMayNull;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Property getFirstProperty() {
        return firstProperty;
    }

    public void setFirstProperty(Property firstProperty) {
        this.firstProperty = firstProperty;
    }

    public Property getSecondProperty() {
        return secondProperty;
    }

    public void setSecondProperty(Property secondProperty) {
        this.secondProperty = secondProperty;
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = (Map<Object, Object>) super.to_json_object();
        Map<String, Object> function = new HashMap<>();
        function.put("method", this.operation);
        function.put("object", this.firstProperty.getName());

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DualVariableMeta that = (DualVariableMeta) o;

        if (getTtl() != that.getTtl()) return false;
        if (!getGroupKeys().equals(that.getGroupKeys())) return false;
        if (!firstId.equals(that.getFirstId())) return false;
        if (!secondId.equals(that.getFirstId())) return false;
        return firstProperty.equals(that.getFirstProperty()) && secondProperty.equals(that.getSecondProperty()) && operation.equals(that.getOperation());

    }

    @Override
    protected void parseFunction(Map<String, Object> functionJson) {
        this.setFirstId(this.getSrcVariableMetasID().get(0));
        this.setSecondId(this.getSrcVariableMetasID().get(1));

        this.setOperation((String) functionJson.get("method"));
        this.setFirstProperty(VariableMetaRegistry.getInstance().getVariableMeta(this.getFirstId()).findPropertyByName((String) functionJson.get("object")));
        this.setSecondProperty(VariableMetaRegistry.getInstance().getVariableMeta(this.getSecondId()).findPropertyByName((String) functionJson.get("object")));

        this.setFirstMayNull((Boolean) functionJson.getOrDefault("first_nullable", true));
        this.setSecondMayNull((Boolean) functionJson.getOrDefault("second_nullable", true));

    }

    @Override
    protected List<Property> parseProperties() {
        List<Property> propertyList = new ArrayList<>();
        if (this.groupKeys != null && groupKeys.size() > 0) {
            this.getGroupKeys().forEach(property -> propertyList.add(new Property(this.getId(), property.getName(), property.getType(), property.getSubType())));
        }
        return propertyList;
    }

    @Override
    protected NamedType parseValueType() {
        // TODO should not restrict to double, although currently operator have just '/'
        return NamedType.DOUBLE;
    }

    public static DualVariableMeta from_json_object(Object obj) {
        DualVariableMeta result = new DualVariableMeta();
        result = from_json_object(obj, result);

        return result;
    }
}
