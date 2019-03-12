package com.threathunter.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Types using in the variable calculating system.
 *
 * <p>Every type is bounded with a name, so users can use a string for a type, not
 * only the java type.
 *
 * <br/>Currently, the supported types are:
 * <ol>
 *     <li>long</li>
 *     <li>double</li>
 *     <li>boolean</li>
 *     <li>string</li>
 *     <li>map</li>
 *     <li>object</li>
 *     <li>list</li>
 * <ol/>
 *
 * created by www.threathunter.cn
 */
public enum NamedType {
    LONG("long", Long.class),
    DOUBLE("double", Double.class),
    BOOLEAN("boolean", Boolean.class),
    STRING("string", String.class),
    MAP("map", Map.class),
    OBJECT("object", Object.class),
    LIST("list", List.class);

    private static Map<String, Class<?>> codeToTypeMap = new HashMap<>();
    private static Map<Class<?>, String> typeToCodeMap = new HashMap<>();

    static {
        // this is executed after the static members are initialized.
        for (NamedType t : NamedType.values()) {
            codeToTypeMap.put(t.code, t.classType);
            typeToCodeMap.put(t.classType, t.code);
        }
    }

    private final String code; // name is used by java, so we call it code.
    private final Class<?> classType;

    private NamedType(String code, Class<?> classType) {
        this.code = code;
        this.classType = classType;
    }

    public String getCode() {
        return code;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public String toString() {
        return code;
    }

    public static String getCodeFromType(Class<?> type) {
        Utility.argumentNotEmpty(type, "no code for null type");

        String result = typeToCodeMap.get(type);
        if (result == null) {
            throw new IllegalStateException("no type found for Class:" + type);
        }

        return result;
    }

    public static Class<?> getTypeFromCode(String code) {
        Utility.argumentNotEmpty(code, "no type for null code");

        Class<?> result = codeToTypeMap.get(code);
        if (result == null) {
            throw new IllegalStateException("no type found for code:" + code);
        }

        return result;
    }

    public static NamedType fromCode(String code) {
        if (Utility.isEmptyStr(code)) {
            throw new IllegalArgumentException("no type for null code");
        }

        for(NamedType nt : NamedType.values()) {
            if (nt.code.equals(code)) {
                return nt;
            }
        }

        return null;
    }

    public static NamedType fromType(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("null type");
        }

        for(NamedType nt : NamedType.values()) {
            if (nt.getClassType().equals(type)) {
                return nt;
            }
        }

        return null;
    }

    public Object to_json_object() {
        return this.code;
    }

    public static NamedType from_json_object(Object object) {
        return fromCode((String)object);
    }
}

