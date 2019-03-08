package com.threathunter.util;

import com.threathunter.model.VariableMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry which uses an object's class as its lookup key.
 *
 * Usually, for each class that inherits from base type A, we may have another related class
 * that inherits from base type B that can work on it. For example, for each specific
 * {@link VariableMeta}, we may have an related {@VariableMetaBuilder}.
 * We should maintain the relationship between the two correlated class families.
 *
 * @param <KeyClass> the base class of the type which is used to look up for the related type
 *                  inherits from ValueClass
 * @param <ValueClass> the base class of the type that is looked up for.
 * @author Wen Lu
 */
public class ClassBasedRegistry<KeyClass, ValueClass> {
    private final Map<Class<? extends KeyClass>, Class<? extends ValueClass>> registry = new
            HashMap<>();
    private final Class<KeyClass> keyClass;

    public ClassBasedRegistry(Class<KeyClass> keyClass) {
        if (keyClass == null) {
            throw new RuntimeException("keyClass is null");
        }
        this.keyClass = keyClass;
    }

    /**
     * Add one mapping.
     *
     * Not thread safe.
     *
     */
    public void register(Class<? extends KeyClass> kc, Class<? extends ValueClass> vc) {
        registry.put(kc, vc);
    }

    /**
     * Get the registered mapping that is based on the key class or its closest ancestor.
     *
     */
    public Class<? extends ValueClass> get(Class<? extends KeyClass> kc) {
        Class<? extends ValueClass> result = null;

        Class<? extends KeyClass> currentClass = kc;
        while (currentClass != null) {
            result = registry.get(currentClass);
            if (result != null) {
                return result;
            }

            Class<?> superClass = currentClass.getSuperclass();
            if (superClass == null || !keyClass.isAssignableFrom(superClass)) {
                break;
            }
            currentClass = (Class<? extends KeyClass>)superClass;
        }

        // not found
        return null;
    }
}
