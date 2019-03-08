package com.threathunter.util;

import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;
import com.threathunter.model.PropertyReduction;

import java.util.ArrayList;
import java.util.List;

import static com.threathunter.common.Utility.isEmptyStr;

/**
 * Some helper methods for operations on {@link Property}
 *
 * @author Wen Lu
 */
public class PropertyHelper {

    /**
     * A list of the property names
     *
     * @param properties
     * @return
     */
    public static List<String> getNameListFromProperties(List<Property> properties) {
        List<String> result = new ArrayList<>();
        if (properties != null) {
            for(Property p : properties) {
                result.add(p.getName());
            }
        }

        return result;
    }

    /**
     * Whether this name is in the properties.
     *
     * @param name
     * @param properties
     * @return
     */
    public static boolean isNameInProperties(String name, List<Property> properties) {
        if (isEmptyStr(name)) {
            return false;
        }

        return getNameListFromProperties(properties).contains(name);
    }

    /**
     * A list of the property names from the destination property of a list of
     * {@link PropertyReduction}.
     *
     */
    public static List<String> getNameListFromPropertyReductions(List<PropertyReduction> reductions) {
        List<String> result = new ArrayList<>();
        if (reductions != null) {
            for(PropertyReduction r : reductions) {
                result.add(r.getDestProperty().getName());
            }
        }

        return result;
    }

    /**
     * Whether this name is in the property reduction's destination.
     *
     */
    public static boolean isNameInPropertyReduction(String name, List<PropertyReduction> reductions) {
        if (isEmptyStr(name)) {
            return false;
        }

        return getNameListFromPropertyReductions(reductions).contains(name);
    }

    /**
     * A list of the property names from the destination property of a list of
     * {@link PropertyMapping}.
     *
     */
    public static List<String> getNameListFromPropertyMappings(List<PropertyMapping> mappings) {
        List<String> result = new ArrayList<>();
        if (mappings != null) {
            for(PropertyMapping m : mappings) {
                result.add(m.getDestProperty().getName());
            }
        }

        return result;
    }

    /**
     * Whether this name is in the property mapping's destination.
     *
     */
    public static boolean isNameInPropertyMapping(String name, List<PropertyMapping> mappings) {
        if (isEmptyStr(name)) {
            return false;
        }

        return getNameListFromPropertyMappings(mappings).contains(name);
    }
}
