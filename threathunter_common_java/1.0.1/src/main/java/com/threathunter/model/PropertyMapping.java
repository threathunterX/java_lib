package com.threathunter.model;

import com.threathunter.common.Utility;
import com.threathunter.config.CommonDynamicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Describing the operations that generating a property from multiple source properties.
 *
 * created by www.threathunter.cn
 */
public abstract class PropertyMapping {
    private static final Map<String, Class<? extends PropertyMapping>> registry = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(PropertyMapping.class);

    public static void init() {
        String[] packages = CommonDynamicConfig.getInstance().getStringArray("nebula.propertymapping.packages");
        if (packages != null) {
            for(String p : packages) {
                Set<Class<? extends PropertyMapping>> classes = Utility.scannerSubTypeFromPackage(p, PropertyMapping.class);
                for(Class cls : classes) {
                    try {
                        int m = cls.getModifiers();
                        if (Modifier.isAbstract(m) || Modifier.isInterface(m)) {
                            continue;
                        }

                        Field f = cls.getDeclaredField("TYPE");
                        String type = (String) f.get(null);
                        addSubClass(type, cls);
                    } catch (Exception ex) {
                        logger.error("init:fatal:fail to process class:" + cls.getName(), ex);
                    }
                }
            }
        }
    }

    protected static void addSubClass(String type, Class<? extends PropertyMapping> cls) {
        registry.put(type, cls);
    }

    protected static Class<? extends PropertyMapping> findSubClass(String type) {
        return registry.get(type);
    }

    private final List<Property> srcProperties;
    private final Property destProperty;
    private final String type;

    // for json
    protected PropertyMapping() {
        srcProperties = null;
        destProperty = null;
        type = null;
    }

    public PropertyMapping(List<Property> srcProperties, Property destProperty, String type) {
        this.srcProperties = srcProperties;
        this.destProperty = destProperty;
        this.type = type;
    }

    public List<Property> getSrcProperties() {
        return srcProperties;
    }

    public Property getDestProperty() {
        return destProperty;
    }

    /**
     * Used to differentiate mappings of different kinds.
     *
     */
    public final String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyMapping that = (PropertyMapping) o;

        if (!getType().equals(that.getType())) return false;
        if (!destProperty.equals(that.destProperty)) return false;
        if (!srcProperties.equals(that.srcProperties)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = srcProperties.hashCode();
        result = 31 * result + destProperty.hashCode();
        result = 31 * result + getType().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PropertyMapping{" +
                "srcProperties=" + srcProperties +
                ", destProperty=" + destProperty +
                ", type='" + type + '\'' +
                '}';
    }

    abstract public Object to_json_object();

    public static PropertyMapping from_json_object(Object obj) {
        try {
            Map<Object, Object> mObj = (Map<Object, Object>)obj;
            String type = (String)mObj.get("type");
            Class cls = findSubClass(type);
            if (cls == null) {
                throw new IllegalStateException("unsupported type: " + type);
            }

            Method method = cls.getMethod("from_json_object", Object.class);
            return (PropertyMapping)method.invoke(null, obj);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
