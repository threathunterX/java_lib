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
 * @author Wen Lu
 */
public class Property<P extends Property> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Property.class);

    private Identifier identifier; // used to describe the belonged event/variable
    private String name;
    private NamedType type;

    public Property(Identifier identifier, String name, NamedType type) {
        // id could be null

        try {
            Utility.argumentNotEmpty(name, "empty property name");
            Utility.argumentNotEmpty(type, "empty property type");

            this.identifier = identifier;
            this.name = name;
            this.type = type;
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
    }

    public String getName() {
        return name;
    }

    public NamedType getType() {
        return type;
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

    public P deepCopy() {
        try {
            P result = (P)this.getClass().newInstance();
            result.setType(type);
            result.setName(name);
            result.setIdentifier(identifier);
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

        return true;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Property{" +
                "identifier=" + identifier +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("name", this.name);
        result.put("type", this.type.getCode());
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
        Object type = mObject.get("type");
        return new Property(id, name, NamedType.from_json_object(type));
    }

    public static Property from_json_object(Object object) {
        Map<Object, Object> mObject = (Map<Object, Object>)(object);
        String name = (String)mObject.get("name");
        Object type = mObject.get("type");
        List<String> keys = (List<String>) mObject.get("identifier");
        Identifier id;
        if (keys == null || keys.isEmpty()) {
            id = null;
        } else {
            id = Identifier.fromKeys(keys);
        }

        return new Property(id, name, NamedType.from_json_object(type));
    }

    public static Property buildLongProperty(Identifier identifier, String name) {
        return new Property(identifier, name, NamedType.LONG);
    }

    public static Property buildLongProperty(String name) {
        return new Property(null, name, NamedType.LONG);
    }

    public static Property buildDoubleProperty(Identifier identifier, String name) {
        return new Property(identifier, name, NamedType.DOUBLE);
    }

    public static Property buildDoubleProperty(String name) {
        return new Property(null, name, NamedType.DOUBLE);
    }

    public static Property buildStringProperty(Identifier identifier, String name) {
        return new Property(identifier, name, NamedType.STRING);
    }

    public static Property buildStringProperty(String name) {
        return new Property(null, name, NamedType.STRING);
    }

    public static Property buildBooleanProperty(Identifier identifier, String name) {
        return new Property(identifier, name, NamedType.BOOLEAN);
    }

    public static Property buildBooleanProperty(String name) {
        return new Property(null, name, NamedType.BOOLEAN);
    }
}
