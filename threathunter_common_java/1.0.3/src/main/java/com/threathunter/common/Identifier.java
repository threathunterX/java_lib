package com.threathunter.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Identifier is broadly used by different kinds of Entity in the system.
 *
 * We usually use multiple strings to identify one instance. This class is used
 * to simplify the work.
 * <br/>
 * The identifier is immutable.
 *
 * @author Wen Lu
 */
public class Identifier {
    private static final Logger logger = LoggerFactory.getLogger(Identifier.class);
    public static final String SEPERATOR = "@"; // used in the unfolded name.

    private final List<String> keys;
    private int hashcode = 0;

    private Identifier(String... keys) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("null keys to create the identifier");
        }

        List<String> tempList = new ArrayList<String>();
        for(String key : keys) {
            if (Utility.isEmptyStr(key) ||
                    (key=key.trim()).isEmpty()) {
                throw new IllegalArgumentException("null key in keys provided to create identifier");
            }
            tempList.add(key);
        }

        this.keys = Collections.unmodifiableList(tempList);
    }

    // for jackson
    private Identifier(){
        this.keys = new ArrayList<>();
    }

    private Identifier(List<String> keys) {
        this(keys.toArray(new String[0]));
    }

    public static Identifier fromKeys(String... keys) {
        return new Identifier(keys);
    }

    public static Identifier fromKeys(List<String> keys) {
        return new Identifier(keys);
    }

    public static Identifier fromUnfoldedName(String unfoldedName) {
        if (Utility.isEmptyStr(unfoldedName)) {
            throw new IllegalArgumentException("null unfoled name");
        }

        return new Identifier(Utility.splitStrings(SEPERATOR, unfoldedName));
    }

    public List<String> getKeys() {
        return keys;
    }

    public String getUnfoldedName() {
        return Utility.joinStrings(SEPERATOR, this.keys);
    }

    @Override
    public String toString() {
        return keys.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Identifier) {
            if (keys.equals(((Identifier) o).keys)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (hashcode == 0) {
            hashcode = keys.hashCode();
        }
        return hashcode;
    }

    public Object to_json_object() {
        return Collections.unmodifiableList(this.keys);
    }

    public static Identifier from_json_object(Object obj) {
        if (obj == null)
            return null;

        List<String> objList = (List<String>) obj;
        if (objList.isEmpty())
            return null;
        return Identifier.fromKeys(objList);
    }
}
