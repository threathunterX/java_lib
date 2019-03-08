package com.threathunter.variable.meta;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyCondition;
import com.threathunter.model.VariableMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by daisy on 18-3-15
 */
public class InternalVariableMeta extends VariableMeta {
    public static final String TYPE = "internal";

    private Identifier id;
    private String app;
    private String name;
    private String visibleName;
    private String status;
    private String module;
    private boolean idDerived = false;
    private List<Property> properties = new ArrayList<>();
    private String remark;
    private String valueType;
    private String valueSubType;

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public String getApp() {
        return this.app;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getType() {
        return "internal";
    }

    @Override
    public String getStatus() {
        return this.status;
    }

    @Override
    public boolean isDerived() {
        return this.idDerived;
    }

    @Override
    public List<Identifier> getSrcVariableMetasID() {
        return new ArrayList<>();
    }

    @Override
    public Identifier getSrcEventMetaID() {
        return null;
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public List<Property> getProperties() {
        return this.properties;
    }

    @Override
    public boolean hasProperty(Property property) {
        Property find = this.findPropertyByName(property.getName());
        if (find == null) {
            return false;
        }
        return find.getType().equals(property.getType());
    }

    @Override
    public Map<String, NamedType> getDataSchema() {
        return null;
    }

    @Override
    public long getExpireDate() {
        return 0;
    }

    @Override
    public long getTtl() {
        return 0;
    }

    @Override
    public Property findPropertyByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (name.equals("value")) {
            return new Property(this.getId(), "value", NamedType.fromCode(getValueType()),
                    (getValueSubType() == null || getValueSubType().isEmpty())? null : NamedType.fromCode(getValueSubType()));
        }

        for(Property p : properties) {
            if (name.equals(p.getName())) {
                return p;
            }
        }
        return null;
    }

    @Override
    public PropertyCondition getPropertyCondition() {
        return null;
    }

    @Override
    public String getRemark() {
        return this.remark;
    }

    @Override
    public String getVisibleName() {
        return this.visibleName;
    }

    @Override
    public String getDimension() {
        return "";
    }

    @Override
    public String getModule() {
        return this.module;
    }

    @Override
    public String getValueType() {
        return this.valueType;
    }

    public String getValueSubType() {
        return this.valueSubType;
    }

    @Override
    public Object to_json_object() {
        return "";
    }

    @Override
    public List<Property> getGroupKeys() {
        return null;
    }

    public static InternalVariableMeta from_json_object(final Object obj) {
        InternalVariableMeta result = new InternalVariableMeta();
        Map<String, Object> map = (Map<String, Object>) obj;
        result.app = (String) map.get("app");
        result.name = (String) map.get("name");
        result.id = Identifier.fromKeys(result.app, result.name);
        result.visibleName = (String) map.get("visible_name");
        result.status = (String) map.get("status");
        result.module = (String) map.get("module");
        result.remark = (String) map.get("remark");
        result.valueType = (String) map.get("value_type");
        result.valueSubType = (String) map.get("value_subtype");

        return result;
    }
}
