package com.threathunter.model;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.common.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event/Variable may both have different properties.
 *
 * <p>One property contains the following members:
 * <ul>
 *     <li>identifier: used to describe the event/variable, could be null if the event/variable
 *     is known in some cases and we don't need to clarify it explicitly.</li>
 *     <li>name: the property name</li>
 *     <li>type: the property type</li>
 * </ul>
 *
 * created by www.threathunter.cn
 */
public class Property<P extends Property> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Property.class);

    private Identifier identifier; // used to describe the belonged event/variable
    private String name;
    private NamedType type;
    private NamedType subType;

    public Property(Identifier identifier, String name, NamedType type, NamedType subType) {
        // id could be null

        try {
            Utility.argumentNotEmpty(name, "empty property name");
            Utility.argumentNotEmpty(type, "empty property type");

            this.identifier = identifier;
            this.name = name;
            this.type = type;
            this.subType = subType;
        } catch (Exception e) {
            LOGGER.error(String.format("id: %s, name: %s", identifier.toString(), name));
            throw new RuntimeException(e);
        }
    }

    // for json and deep copy
    private Property() {
        identifier = null;
        name = null;
        type = null;
        subType = null;
    }

    public String getName() {
        return name;
    }

    public NamedType getType() {
        return type;
    }

    public NamedType getSubType() {
        return subType;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(NamedType type) {
        this.type = type;
    }

    public void setSubType(NamedType type) {
        this.subType = type;
    }

    public P deepCopy() {
        try {
            P result = (P)this.getClass().newInstance();
            result.setType(type);
            result.setName(name);
            result.setIdentifier(identifier);
            result.setSubType(subType);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("can't instantiate the clone object", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Property property = (Property) o;

        if (identifier != null ? !identifier.equals(property.identifier) : property.identifier != null) return false;
        if (!name.equals(property.name)) return false;
        if (type != property.type) return false;
        if (subType != property.subType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (subType == null ? "".hashCode() : subType.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Property{" +
                "identifier=" + identifier +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", subtype=" + subType +
                '}';
    }

    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("name", this.name);
        result.put("type", this.type.getCode());
        result.put("subtype", this.subType == null ? "" : this.subType.getCode());
        if (this.identifier != null) {
            result.put("identifier", this.identifier.to_json_object());
        } else {
            result.put("identifier", null);
        }
        return result;
    }

    public static Property from_json_object(Object object, Identifier id) {
        Map<Object, Object> mObject = (Map<Object, Object>)(object);
        String name = (String)mObject.get("name");
        String type = (String) mObject.get("type");
        String subType = (String) mObject.getOrDefault("subtype", "");
        return new Property(id, name, NamedType.from_json_object(type), subType.isEmpty() ? null : NamedType.from_json_object(subType));
    }

    public static Property from_json_object(Object object) {
        Map<Object, Object> mObject = (Map<Object, Object>)(object);
        String name = (String)mObject.get("name");
        NamedType type = NamedType.from_json_object(mObject.get("type"));
        String subTypeString = (String) mObject.getOrDefault("subtype", "");
        NamedType subType = subTypeString.isEmpty() ? null : NamedType.from_json_object(subTypeString);
        List<String> keys = (List<String>) mObject.get("identifier");
        Identifier id;
        if (keys == null || keys.isEmpty()) {
            id = null;
        } else {
            id = Identifier.fromKeys(keys);
        }

        return new Property(id, name, type, subType);
    }

    public static Property buildLongProperty(Identifier identifier, String name) {
        return new Property(identifier, name, NamedType.LONG, null);
    }

    public static Property buildLongProperty(String name) {
        return new Property(null, name, NamedType.LONG, null);
    }

    public static Property buildDoubleProperty(Identifier identifier, String name) {
        return new Property(identifier, name, NamedType.DOUBLE, null);
    }

    public static Property buildDoubleProperty(String name) {
        return new Property(null, name, NamedType.DOUBLE, null);
    }

    public static Property buildStringProperty(Identifier identifier, String name) {
        return new Property(identifier, name, NamedType.STRING, null);
    }

    public static Property buildStringProperty(String name) {
        return new Property(null, name, NamedType.STRING, null);
    }

    public static Property buildBooleanProperty(Identifier identifier, String name) {
        return new Property(identifier, name, NamedType.BOOLEAN, null);
    }

    public static Property buildBooleanProperty(String name) {
        return new Property(null, name, NamedType.BOOLEAN, null);
    }
}
