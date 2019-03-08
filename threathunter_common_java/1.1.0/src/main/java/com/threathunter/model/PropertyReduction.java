package com.threathunter.model;

import com.threathunter.common.Utility;
import com.threathunter.config.CommonDynamicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Get the reduction value as a new property from one existing property.
 *
 * @author Wen Lu
 */
public abstract class PropertyReduction {
    private static final Map<String, Class<? extends PropertyReduction>> registry = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(PropertyReduction.class);

    public static void init() {
        String[] packages = CommonDynamicConfig.getInstance().getStringArray("variable.meta.property.reduction.packages");
        if (packages == null || packages.length <= 0) {
            packages = new String[] { "com.threathunter" };
        }
        for(String p : packages) {
            Set<Class<? extends PropertyReduction>> classes = Utility.scannerSubTypeFromPackage(p, PropertyReduction.class);
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
                    logger.error("fatal:init:fail to process class:" + cls.getName(), ex);
                }
            }
        }
    }

    protected static void addSubClass(String type, Class<? extends PropertyReduction> cls) {
        registry.put(type, cls);
    }

    protected static Class<? extends PropertyReduction> findSubClass(String type) {
        return registry.get(type);
    }

    private final List<Property> srcProperties;
    private final Property destProperty;
    private final String type;
    private final String method;

    // for json
    protected PropertyReduction() {
        srcProperties = null;
        destProperty = null;
        type = null;
        method = null;
    }

    public PropertyReduction(List<Property> srcProperties, Property destProperty, String type, String method) {
        this.srcProperties = srcProperties;
        this.destProperty = destProperty;
        this.type = type;
        this.method = method;
    }

    public PropertyReduction(Property srcProperty, Property destProperty, String type, String method) {
        this(Arrays.asList(srcProperty), destProperty, type, method);
    }

    public List<Property> getSrcProperties() {
        return srcProperties;
    }

    public Property getDestProperty() {
        return destProperty;
    }

    /**
     * Used to differentiate reductions of different kinds.
     *
     */
    public final String getType() {
        return type;
    }

    public final String getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyReduction that = (PropertyReduction) o;

        if (!destProperty.equals(that.destProperty)) return false;
        if (!srcProperties.equals(that.srcProperties)) return false;
        if (!getType().equals(that.getType())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = srcProperties.hashCode();
        result = 31 * result + destProperty.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PropertyReduction{" +
                "srcProperties=" + srcProperties +
                ", destProperty=" + destProperty +
                ", type='" + type + '\'' +
                '}';
    }

    abstract public Object to_json_object();

    public static PropertyReduction from_json_object(Object obj) {
        try {
            Map<Object, Object> mObj = (Map<Object, Object>)obj;
            String type = (String)mObj.get("type");
            Class cls = findSubClass(type);
            if (cls == null) {
                throw new IllegalStateException("unsupported type: " + type);
            }

            Method method = cls.getMethod("from_json_object", Object.class);
            return (PropertyReduction)method.invoke(null, obj);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
