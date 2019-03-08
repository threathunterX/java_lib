package com.threathunter.variable.condition;

import com.threathunter.common.Identifier;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyCondition;
import com.threathunter.model.VariableMeta;
import com.threathunter.model.VariableMetaRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daisy on 17-9-1
 */
public class ConditionParser {

    public static Map<String, Object> parseFrom(Map<String, Object> unparse, String app, Identifier defaultParentId) {
        if (unparse == null || unparse.isEmpty()) {
            return null;
        }

        List<Object> conditions = new ArrayList<>();
        if (unparse.get("type").equals("simple")) {
            String parent = (String) unparse.get("source");
            if (parent == null || parent.isEmpty()) {
                return genConditionMap(unparse, defaultParentId);
            }
            else {
                return genConditionMap(unparse, Identifier.fromKeys(app, parent));
            }
        } else {
            ((List<Map<String, Object>>) unparse.get("condition")).forEach(map -> {
                conditions.add(parseFrom(map, app, defaultParentId));
            });
        }
        Map<String, Object> result = new HashMap<>();

        result.put("condition", conditions);
        result.put("type", unparse.get("type"));

        return result;

    }

    private static Map<String, Object> genConditionMap(Map<String, Object> conditionMap, Identifier srcId) {
        VariableMeta srcMeta = VariableMetaRegistry.getInstance().getVariableMeta(srcId);
        String propertyName = (String) conditionMap.get("object");
        String operation = (String) conditionMap.get("operation");
        Object value = conditionMap.get("value");

        Map<String, Object> result = new HashMap<>();
        Property srcProperty = srcMeta.findPropertyByName(propertyName);
        String conditionType = String.format("%s%s", srcProperty.getType().getCode(), operation);

        result.put("type", conditionType);
        result.put("srcProperty", srcProperty.to_json_object());
        result.put("param", value);
        if (operation.contains("location")) {
            result.put("paramType", conditionMap.get("param"));
        }

        return result;
    }

    // eras the information about type and src
    public static Map<String, Object> toJsonObject(PropertyCondition condition) {
        if (condition == null) {
            return null;
        }
        if (!(condition instanceof CompoundCondition)) {
            return toConditionMap(condition);
        }
        Map<String, Object> result = new HashMap<>();
        List<Object> cons = new ArrayList<>();
        CompoundCondition compoundCondition = (CompoundCondition) condition;
        compoundCondition.getConditions().forEach(con -> {
            cons.add(toJsonObject(con));
        });

        result.put("condition", cons);
        result.put("type", compoundCondition.getType());
        return result;
    }

    private static Map<String, Object> toConditionMap(PropertyCondition condition) {
        Map<String, Object> result = new HashMap<>();
        Property property = condition.getSrcProperties().get(0);

        String fullType = condition.getType();
        result.put("operation", fullType.substring(property.getType().getCode().length(), fullType.length()));
        result.put("value", condition.getParam());
        result.put("object", property.getName());
        result.put("type", "simple");

        return result;
    }
}
