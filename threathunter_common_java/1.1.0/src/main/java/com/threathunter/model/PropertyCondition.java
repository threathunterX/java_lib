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
 * PropertyCondition defines the operation for testing on properties.
 *
 * @author Wen Lu
 */
public abstract class PropertyCondition {
    private static final Map<String, Class<? extends PropertyCondition>> registry = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(PropertyCondition.class);

    public static void init() {
        String[] packages = CommonDynamicConfig.getInstance().getStringArray("variable.meta.property.condition.packages");
        if (packages == null || packages.length <= 0) {
            packages = new String[] { "com.threathunter" };
        }
        for(String p : packages) {
            Set<Class<? extends PropertyCondition>> classes = Utility.scannerSubTypeFromPackage(p, PropertyCondition.class);
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

    protected static void addSubClass(String type, Class<? extends PropertyCondition> cls) {
        registry.put(type, cls);
    }

    protected static Class<? extends PropertyCondition> findSubClass(String type) {
        return registry.get(type);
    }

    /**
     * Each kind of {@link PropertyCondition} uses a string field {@code type} to
     * differentiate itself from other kinds.
     */
    private final String type;

    // for json
    protected PropertyCondition() {
        type = "";
    }

    public PropertyCondition(String type) {
        this.type = type;
    }

    /**
     * Get the properties this condition is based on.
     *
     */
    abstract public List<Property> getSrcProperties();
    
    abstract public Object getParam();

    /**
     * Used to differentiate conditions of different kinds.
     *
     */
    public final String getType() {
        return type;
    }

    abstract public Object to_json_object();

    public static PropertyCondition from_json_object(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            Map<Object, Object> mObj = (Map<Object, Object>)obj;
            String type = (String)mObj.get("type");
            Class cls = findSubClass(type);
            if (cls == null) {
                throw new IllegalStateException("unsupported type: " + type);
            }

            Method method = cls.getMethod("from_json_object", Object.class);
            return (PropertyCondition)method.invoke(null, obj);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
