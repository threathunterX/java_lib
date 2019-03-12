package com.threathunter.variable.json;

import com.threathunter.common.Utility;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A registry that can get the class object from the type name.
 *
 * In many cases(like variable meta, event meta), there are many subclasses, each
 * may have a static field called "TYPE" to identify itself. We need to use
 * this information to find the related class, which will be very useful in
 * the deserialization of json.
 *
 * created by www.threathunter.cn
 */
public class TypeToClassRegistry<T> {
    private Map<String, Class<? extends T>> registry =
            new HashMap<>();
    private Class<T> typeClass;

    /**
     * Construct the registry.
     *
     * @param defaultPackage default package that will be first checked
     * @param systemProperty user can use system property to define the
     *                       optional packages, which are separated by comma.
     * @param typeClass the base class of this registry.
     */
    public TypeToClassRegistry(String defaultPackage, String systemProperty, Class<T> typeClass) {
        this.typeClass = typeClass;
        List<String> typePackages = new ArrayList<>();
        typePackages.add(defaultPackage);// the default package

        // read the system defined ones
        if (!Utility.isEmptyStr(systemProperty)) {
            String userDefined = System.getProperty(systemProperty);
            if (userDefined != null) {
                for(String p : userDefined.split(",")) {
                    typePackages.add(p);
                }
            }
        }

        // find all the types
        for(String p : typePackages) {
            addPackage(p);
        }
    }

    /**
     * Construct the registry.
     *
     * @param defaultPackage default package that will be first checked
     * @param typeClass the base class of this registry.
     */
    public TypeToClassRegistry(String defaultPackage, Class<T> typeClass) {
        this(defaultPackage, null, typeClass);
    }

    /**
     * Get the class type from the type name.
     *
     * @param type name of the type.
     * @return the related class type.
     */
    public Class<? extends T> getTypeClass(String type) {
        return registry.get(type);
    }

    /**
     * Return all data in the internal registry.
     *
     * @return
     */
    public Map<String, Class<? extends T>> getAllTypeClasses() {
        return registry;
    }

    /**
     * Add a specific type class
     * @param type type name of the class
     * @param typeClass the class to be added
     */
    public void addTypeClass(String type, Class<? extends T> typeClass) {
        registry.put(type, typeClass);
    }

    /**
     * Add all subclasses in this package to the registry.
     *
     * @param packageName the packages where we can find the subclasses.
     */
    public void addPackage(String packageName) {
        Set<Class<? extends T>> subclasses = Utility.scannerSubTypeFromPackage(packageName, typeClass);
        if (subclasses == null) return;
        for(Class<? extends T> subclass : subclasses) {
            try {
                int modifier = subclass.getModifiers();
                if (Modifier.isInterface(modifier) ||
                        Modifier.isAbstract(modifier)) {
                    continue;
                }
                Field f = subclass.getField("TYPE");
                String type = (String)f.get(null);
                registry.put(type, subclass);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}
