package com.threathunter.variable.meta;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.*;
import com.threathunter.variable.condition.ConditionParser;

import java.util.*;

import static com.threathunter.variable.json.JsonUtil.property_list_from_json_object;

/**
 * Created by daisy on 17-9-24
 */
public abstract class BaseVariableMeta extends VariableMeta {
    public static final String TYPE = "base";
    static {
        VariableMeta.addSubClass(TYPE, BaseVariableMeta.class);
    }

    // every mata has the following fixed-type fields
    private static final Map<String, Property> FIXEDSCHEMAS = Collections.synchronizedMap(
            new HashMap<String, Property>());
    static {
        FIXEDSCHEMAS.put("app", Property.buildStringProperty("app"));
        FIXEDSCHEMAS.put("name", Property.buildStringProperty("name"));
        FIXEDSCHEMAS.put("key", Property.buildStringProperty("key"));
        FIXEDSCHEMAS.put("timestamp", Property.buildLongProperty("timestamp"));
    }

    protected Identifier id;
    protected String app;
    protected String name;
    protected String type;
    protected String status;
    protected boolean isDerived = false;
    protected List<Identifier> srcVariableMetasID = new ArrayList<>();
    protected Identifier srcEventMetaID = null;
    protected boolean isInternal;
    protected int priority = 0;
    protected List<Property> properties = Collections.synchronizedList(new ArrayList<Property>());
    protected long expireDate = 0;
    protected long ttl = 0;
    protected String remark;
    protected String visibleName;
    protected String dimension;
    protected String module;
    protected NamedType valueType;
    protected List<Property> groupKeys;
    protected Map<String, NamedType> dataSchema;

    protected List<PropertyMapping> mappings;
    protected PropertyReduction reduction;
    protected PropertyCondition condition;

    public void setId(Identifier id) {
        this.id = id;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDerived(boolean derived) {
        isDerived = derived;
    }

    public void setSrcVariableMetasID(List<Identifier> srcVariableMetasID) {
        this.srcVariableMetasID = srcVariableMetasID;
    }

    public void setSrcEventMetaID(Identifier srcEventMetaID) {
        this.srcEventMetaID = srcEventMetaID;
    }

    public void setInternal(boolean internal) {
        isInternal = internal;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setVisibleName(String visibleName) {
        this.visibleName = visibleName;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void setValueType(NamedType valueType) {
        this.valueType = valueType;
    }

    public void setGroupKeys(List<Property> groupKeys) {
        this.groupKeys = groupKeys;
    }

    public void setDataSchema(Map<String, NamedType> dataSchema) {
        this.dataSchema = dataSchema;
    }

    public void setMappings(List<PropertyMapping> mappings) {
        this.mappings = mappings;
    }

    public void setReduction(PropertyReduction reduction) {
        this.reduction = reduction;
    }

    public void setCondition(PropertyCondition condition) {
        this.condition = condition;
    }

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
        return this.type;
    }

    @Override
    public String getStatus() {
        return this.status;
    }

    @Override
    public boolean isDerived() {
        return this.isDerived;
    }

    @Override
    public List<Identifier> getSrcVariableMetasID() {
        return this.srcVariableMetasID;
    }

    @Override
    public Identifier getSrcEventMetaID() {
        return this.srcEventMetaID;
    }

    @Override
    public boolean isInternal() {
        return this.isInternal;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public List<Property> getProperties() {
        return properties;
    }

    @Override
    public boolean hasProperty(Property property) {
        if (property == null) {
            return false;
        }
        NamedType type = dataSchema.get(property.getName());
        return type != null && type.equals(property.getType()) ? true : false;
    }

    @Override
    public Map<String, NamedType> getDataSchema() {
        return this.dataSchema;
    }

    @Override
    public long getExpireDate() {
        return this.expireDate;
    }

    @Override
    public long getTtl() {
        return this.ttl;
    }

    @Override
    public Property findPropertyByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (name.equals("value")) {
            return new Property(this.getId(), "value", NamedType.fromCode(getValueType()));
        }

        for(Property p : properties) {
            if (name.equals(p.getName())) {
                return p;
            }
        }

        try {
            Property result = FIXEDSCHEMAS.get(name).deepCopy();
            result.setIdentifier(getId());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        return this.dimension;
    }

    @Override
    public String getModule() {
        return this.module;
    }

    @Override
    public String getValueType() {
        return valueType.getCode();
    }

    @Override
    public List<PropertyMapping> getPropertyMappings() {
        return mappings;
    }

    @Override
    public PropertyCondition getPropertyCondition() {
        return condition;
    }

    @Override
    public PropertyReduction getPropertyReduction() {
        return reduction;
    }

    @Override
    public List<Property> getGroupKeys() {
        return this.groupKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseVariableMeta that = (BaseVariableMeta) o;

        if (priority != that.getPriority()) return false;
        if (expireDate != that.getExpireDate()) return false;
        if (ttl != that.getTtl()) return false;
        if (isInternal != that.isInternal()) return false;
        if (!app.equals(that.getApp())) return false;
        if (!name.equals(that.getName())) return false;
        if (!type.equals(that.getType())) return false;
        if (srcVariableMetasID != null ? !srcVariableMetasID.equals(that.getSrcVariableMetasID()) : that.getSrcVariableMetasID() != null)
            return false;
        if (srcEventMetaID != null ? !srcEventMetaID.equals(that.getSrcEventMetaID()) : that.getSrcEventMetaID() != null) return false;
        return !(properties != null ? !properties.equals(that.getProperties()) : that.getProperties() != null);

    }

    @Override
    public int hashCode() {
        int result = app.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (srcVariableMetasID != null ? srcVariableMetasID.hashCode() : 0);
        result = 31 * result + (srcEventMetaID != null ? srcEventMetaID.hashCode() : 0);
        result = 31 * result + priority;
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (int) (expireDate ^ (expireDate >>> 32));
        result = 31 * result + (int) (ttl ^ (ttl >>> 32));
        result = 31 * result + (isInternal ? 1 : 0);
        return result;
    }

    protected static Map<String, NamedType> genDataSchema(List<Property> properties, NamedType valueType) {
        Map<String, NamedType> result = new HashMap<>();
        for(Property p : properties) {
            result.put(p.getName(), p.getType());
        }

        // add the fixed ones.
        for(String key : FIXEDSCHEMAS.keySet()) {
            result.put(key, FIXEDSCHEMAS.get(key).getType());
        }

        result.put("value", valueType);
        return result;
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("app", getApp());
        result.put("name", getName());
        result.put("type", getType());
        result.put("module", getModule());
        result.put("remark", getRemark());
        result.put("visible_name", getVisibleName());
        result.put("dimension", getDimension());
        result.put("status", getStatus());
        List<Map<String, String>> srcs = new ArrayList<>();
        if (getSrcEventMetaID() != null) {
            Map<String, String> map = new HashMap<>();
            map.put("app", getSrcEventMetaID().getKeys().get(0));
            map.put("sourcename", getSrcEventMetaID().getKeys().get(1));
            srcs.add(map);
        } else {
            getSrcVariableMetasID().forEach(id -> {
                Map<String, String> map = new HashMap<>();
                map.put("app", id.getKeys().get(0));
                map.put("sourcename", id.getKeys().get(1));
                srcs.add(map);
            });
        }
        result.put("source", srcs);
        Map<String, Object> periodMap = new HashMap<>();
        periodMap.put("period_value", this.getTtl());
        periodMap.put("type", "last_seconds");
        result.put("period", periodMap);
        result.put("filter", new HashMap<>());
        result.put("function", new HashMap<>());
        result.put("groupbykeys", IndexParser.toJsonObject(this.groupKeys));
        return result;
    }

    protected static <T extends BaseVariableMeta> T from_json_object(Object obj, T result) {
        Map<Object, Object> map = (Map<Object, Object>)obj;
        result.setId((Identifier) map.get("identifier"));
        result.setApp((String) map.get("app"));
        result.setName((String) map.get("name"));
        result.setType((String) map.get("type"));
        result.setStatus((String) map.get("status"));
        if (result.getType().equals("event")) {
            result.setSrcEventMetaID(((List<Identifier>) map.get("parents")).get(0));
            result.setDerived(false);
        } else {
            result.setSrcVariableMetasID((List<Identifier>) map.get("parents"));
            result.setDerived(true);
        }
        result.setInternal((Boolean) map.getOrDefault("internal", false));
        result.setPriority((Integer) map.get("priority"));

        result.setExpireDate(((Number) map.getOrDefault("expire", 0)).longValue());
        result.setTtl(((Number) map.getOrDefault("period", 300)).longValue());
        result.setRemark((String) map.get("remark"));
        result.setVisibleName((String) map.get("visible_name"));
        result.setDimension((String) map.get("dimension"));
        result.setModule((String) map.get("module"));

        List<String> groupKeys = (List<String>) map.get("groupkeys");
        Map<String, Object> function = (Map<String, Object>) map.get("function");
        if (function != null && function.get("method") != null && function.get("method").equals("group_count")) {
            groupKeys.add((String) function.get("object"));
        }
        if (groupKeys != null && groupKeys.size() > 0) {
            result.setGroupKeys(property_list_from_json_object(IndexParser.parseFrom((List<String>) map.get("groupkeys"), result.getSrcVariableMetasID().get(0))));
        }

        if (!result.getType().equals("event")) {
            result.setCondition(PropertyCondition.from_json_object(ConditionParser.parseFrom((Map<String, Object>) map.get("condition"), result.getApp(), result.getSrcVariableMetasID().get(0))));
        }
        result.initialFunction((Map<String, Object>) map.get("function"));

        result.setProperties(result.parseProperties());
        result.setValueType(result.getPropertyReduction() == null ?
                NamedType.DOUBLE : result.getPropertyReduction().getDestProperty().getType());

        result.setDataSchema(result.genDataSchema(result.getProperties(), NamedType.fromCode(result.getValueType())));

        return result;
    }

    protected abstract void initialFunction(Map<String, Object> functionJson);
    protected abstract List<Property> parseProperties();
}
