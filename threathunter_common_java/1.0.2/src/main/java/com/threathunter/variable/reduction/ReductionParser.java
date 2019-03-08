package com.threathunter.variable.reduction;

import com.threathunter.common.Identifier;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyReduction;
import com.threathunter.model.VariableMeta;
import com.threathunter.model.VariableMetaRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daisy on 17-9-3
 */
public class ReductionParser {
    public static Map<String, Object> parseFrom(Map<String, Object> function, Identifier srcId, Identifier id) {
        if (function == null || function.isEmpty()) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        String method = (String) function.get("method");
        String propertyName = (String) function.getOrDefault("object", "value");
        if (propertyName.isEmpty()) {
            propertyName = "value";
        }
        VariableMeta srcMeta = VariableMetaRegistry.getInstance().getVariableMeta(srcId);

        Property srcProperty = srcMeta.findPropertyByName(propertyName);
        Map<String, Object> destMap = new HashMap<>();
        destMap.put("identifier", new ArrayList<>());
        destMap.put("type", "object");
        destMap.put("name", "value");
        result.put("srcProperty", srcProperty.to_json_object());
        result.put("destProperty", destMap);
        String subType = (String) function.getOrDefault("object_subtype", "");
        if (!subType.isEmpty()) {
            result.put("type", subType + method);
        } else {
            result.put("type", srcProperty.getType() + method);
        }
        result.put("param", function.get("param"));

        return result;
    }

    public static Map<String, Object> toJsonObject(PropertyReduction reduction) {
        Map<String, Object> result = new HashMap<>();
        Property property = reduction.getSrcProperties().get(0);
        String type = reduction.getType();
        result.put("method", type.substring(property.getType().getCode().length(), type.length()));
        result.put("object", property.getName());
        return result;
    }
}
