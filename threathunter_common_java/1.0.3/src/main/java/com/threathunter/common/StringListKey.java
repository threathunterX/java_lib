package com.threathunter.common;

import java.util.Arrays;

/**
 * Contains a group of String and could be used as key in map.
 *
 * Provide hashCode and equals for a list of string, so that they can be used as
 * key in {@link java.util.Map}. The list of string will not be able to be modified
 * once it is created.
 *
 * created by www.threathunter.cn
 */
public class StringListKey {
    private String[] data;
    private int hash = 0; // buffer the hash code, so we only need to calculate once.

    public StringListKey(String... data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("null data for StringListKey");
        }

        for(String str : data) {
            if (Utility.isEmptyStr(str)) {
                throw new IllegalArgumentException("null string in StringListKey");
            }
        }

        this.data = Arrays.copyOf(data, data.length);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            for(String str : data) {
                hash = hash*31 + str.hashCode();
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringListKey) {
            StringListKey other = (StringListKey)obj;
            if (this.hash == other.hash && this.data.length == other.data.length) {
                boolean equals = true;
                for (int i = 0; i < this.data.length; i++) {
                    if (this.data[i].equals(other.data[i])) {
                        equals = false;
                        break;
                    }
                }
                if (equals) return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
