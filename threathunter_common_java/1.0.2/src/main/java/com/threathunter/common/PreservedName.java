package com.threathunter.common;

import java.util.*;

/**
 * created by www.threathunter.cn
 */
public class PreservedName {
    public static final Set<String> preservedNames = Collections.unmodifiableSet(
            new HashSet<String>() {
                {
                    add("Value");
                    add("name");
                    add("key");
                    add("timestamp");
                    add("value");
                }
            }
    );

    public static boolean isPreservedName(String name) {
        return preservedNames.contains(name.toLowerCase());
    }

    public static List<String> getAllPreservedNames() {
        return new ArrayList<String>(preservedNames);
    }
}
