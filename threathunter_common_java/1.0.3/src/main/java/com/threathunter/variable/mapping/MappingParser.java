package com.threathunter.variable.mapping;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;
import com.threathunter.model.VariableMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class MappingParser {

    public static Map<String, Object> parseFrom(Map<String, Object> unparse, VariableMeta srcMeta, Identifier targetId) {
        Property srcProperty = srcMeta.findPropertyByName((String) unparse.get("src"));
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> srcMap = new HashMap<>();
        srcMap.put("identifier", srcProperty.getIdentifier().getKeys());
        srcMap.put("name", srcProperty.getName());
        srcMap.put("type", srcProperty.getType().getCode());

        Map<String, Object> targetMap = new HashMap<>();
        targetMap.put("identifier", targetId.getKeys());
        targetMap.put("name", unparse.get("dest"));
        targetMap.put("type", NamedType.OBJECT.getCode());

        result.put("type", unparse.get("type"));
        result.put("srcProperty", srcMap);
        result.put("destProperty", targetMap);

        return result;
    }

    public static Map<String, Object> genDirect(String name, VariableMeta srcMeta, Identifier targetId) {
        Property srcProperty = srcMeta.findPropertyByName(name);
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> srcMap = new HashMap<>();
        srcMap.put("identifier", srcProperty.getIdentifier().getKeys());
        srcMap.put("name", srcProperty.getName());
        srcMap.put("type", srcProperty.getType().getCode());

        Map<String, Object> targetMap = new HashMap<>();
        targetMap.put("identifier", targetId.getKeys());
        targetMap.put("name", name);
        targetMap.put("type", NamedType.OBJECT.getCode());

        result.put("type", "direct");
        result.put("srcProperty", srcMap);
        result.put("destProperty", targetMap);

        return result;

    }

    public static Map<String, String> toJsonObject(PropertyMapping mapping) {
        Map<String, String> result = new HashMap<>();
        result.put("type", mapping.getType());
        result.put("src", mapping.getSrcProperties().get(0).getName());
        result.put("dest", mapping.getDestProperty().getName());
        return result;
    }
}
