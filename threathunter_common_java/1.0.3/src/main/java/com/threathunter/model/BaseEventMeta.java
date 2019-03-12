package com.threathunter.model;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.common.Utility;

import java.util.*;

/**
 * created by www.threathunter.cn
 */
public class BaseEventMeta implements EventMeta {
    public static final String TYPE = "base";
    private static final Map<String, NamedType> FIXEDSCHEMAS = Collections.synchronizedMap(
            new HashMap<String, NamedType>());
    static {
        FIXEDSCHEMAS.put("app", NamedType.STRING);
        FIXEDSCHEMAS.put("name", NamedType.STRING);
        FIXEDSCHEMAS.put("key", NamedType.STRING);
        FIXEDSCHEMAS.put("timestamp", NamedType.LONG);
        FIXEDSCHEMAS.put("value", NamedType.DOUBLE);
    }

    private String app;
    private String name;
    private String type;
    private boolean derived;
    private Identifier srcVariableID;
    private List<Property> properties = Collections.synchronizedList(new ArrayList<Property>());
    private long expire = -1; // for ever
    private String remark = "";

    @Override
    public String getApp() {
        return app;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean isDerived() {
        return derived;
    }

    @Override
    public Identifier getSrcVariableID() {
        return srcVariableID;
    }

    @Override
    public boolean hasProperty(Property property) {
        if (property == null) {
            return false;
        }

        for(Property p : properties) {
            if (p.getName().equals(property.getName()) &&
                    p.getType().equals(property.getType())) {
                return true;
            }
        }
        for(Map.Entry<String, NamedType> entry : FIXEDSCHEMAS.entrySet()) {
            String key = entry.getKey();
            NamedType type = entry.getValue();
            if (key.equals(property.getName()) &&
                    type.equals(property.getType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Property> getProperties() {
        return properties;
    }


    @Override
    public Map<String, NamedType> getDataSchema() {
        Map<String, NamedType> result = new HashMap<String, NamedType>();
        for(Property p : properties) {
            result.put(p.getName(), p.getType());
        }

        result.putAll(FIXEDSCHEMAS);
        return result;
    }

    @Override
    public long expireAt() {
        return expire;
    }

    @Override
    public void active() {
        EventMetaRegistry.getInstance().addEventMeta(this);
    }

    @Override
    public void deactive() {
    }

    @Override
    public String getRemark() {
        return remark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseEventMeta that = (BaseEventMeta) o;

        if (derived != that.derived) return false;
        if (!app.equals(that.app)) return false;
        if (!name.equals(that.name)) return false;
        if (!type.equals(that.type)) return false;
        if (srcVariableID != null ? !srcVariableID.equals(that.srcVariableID) : that.srcVariableID != null)
            return false;
        return properties.equals(that.properties);

    }

    @Override
    public int hashCode() {
        int result = app.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (derived ? 1 : 0);
        result = 31 * result + (srcVariableID != null ? srcVariableID.hashCode() : 0);
        result = 31 * result + properties.hashCode();
        return result;
    }

    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("app", getApp());
        result.put("name", getName());
        result.put("type", getType());
        result.put("derived", isDerived());
        if (this.srcVariableID != null) {
            result.put("srcVariableID", this.srcVariableID.to_json_object());
        } else {
            result.put("srcVariableID", null);
        }
        List<Object> propertiesObj = new ArrayList<>();
        if (this.properties != null) {
            for(Property p : this.properties) {
                propertiesObj.add(p.to_json_object());
            }
        }
        result.put("properties", propertiesObj);
        result.put("expire", expireAt());
        result.put("remark", remark);
        return result;
    }

    public static BaseEventMeta from_json_object(Object obj) {
        Map<Object, Object> map = (Map<Object, Object>)obj;
        BaseEventMeta result = new BaseEventMeta();
        result.app = (String)map.get("app");
        result.name = (String)map.get("name");
        result.type = (String)map.get("type");
        Object srcVariableID = map.get("srcVariableID");
        if (srcVariableID == null) {
            result.srcVariableID = null;
        } else {
            result.srcVariableID = Identifier.from_json_object(srcVariableID);
        }
        result.properties = new ArrayList<>();
        List<Object> propertiesObj = (List<Object>)map.get("properties");
        Identifier id = Identifier.fromKeys(result.getApp(), result.getName());
        if (propertiesObj != null) {
            for(Object o : propertiesObj) {
                result.properties.add(Property.from_json_object(o, id));
            }
        }
        result.remark = (String) map.get("remark");
        return result;
    }

    public static BaseEventMetaBuilder builder() {
        return new BaseEventMetaBuilder(new BaseEventMeta());
    }

    public static class BaseEventMetaBuilder {
        private BaseEventMeta event;

        protected BaseEventMetaBuilder(BaseEventMeta event) {
            this.event = event;
            event.type = TYPE;
        }

        public BaseEventMetaBuilder setApp(String app) {
            event.app = app;
            return this;
        }

        public BaseEventMetaBuilder setName(String name) {
            event.name = name;
            return this;
        }

        public BaseEventMetaBuilder setDerived(boolean derived) {
            event.derived = derived;
            return this;
        }

        public BaseEventMetaBuilder setRemark(String remark) {
            event.remark = remark;
            return this;
        }

        public BaseEventMetaBuilder setSrcVariableID(Identifier srcVariableID) {
            event.srcVariableID = srcVariableID;
            return this;
        }

        public BaseEventMetaBuilder setProperties(List<Property> properties) {
            if (properties == null) {
                throw new IllegalArgumentException("null properties");
            }

            // sanity check
            Set<String> tempSet = new HashSet<String>();
            for(Property p : properties) {
                String propertyName = p.getName();
                if (tempSet.contains(p)) {
                    throw new IllegalStateException("there are properties with the same name");
                }

                tempSet.add(propertyName);
            }

            event.properties = Collections.synchronizedList(properties);
            return this;
        }

        public BaseEventMetaBuilder setExpire(long expire) {
            event.expire = expire;
            return this;
        }

        public BaseEventMeta build() {
            if (Utility.isEmptyStr(event.app) || Utility.isEmptyStr(event.name)
                    || Utility.isEmptyStr(event.type)) {
                throw new RuntimeException("null identifier");
            }

            if (event.derived) {
                if (event.srcVariableID == null) {
                    throw new RuntimeException("src variable should not be null");
                }
            }
            EventMetaRegistry.getInstance().addEventMeta(event);
            return event;
        }
    }
}
