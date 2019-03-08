package com.threathunter.model;

import com.threathunter.common.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Single registry for variable meta.
 *
 * Variable meta is identified by its belonged app and own name, we need a
 * registry to help us find the meta detail by the app and name.
 *
 * @author Wen Lu
 */
public class VariableMetaRegistry {

    // storing all the metas
    private volatile ConcurrentMap<Identifier, VariableMeta> map = new ConcurrentHashMap<>();
    // store the number of non-internal variables
    private volatile int nonInternalCount = 0;

    // singleton
    private static final VariableMetaRegistry instance = new VariableMetaRegistry();

    private VariableMetaRegistry(){}

    public static VariableMetaRegistry getInstance() {
        return instance;
    }

    /**
     * Get variable meta identified by the app and variable name.
     *
     */
    public VariableMeta getVariableMeta(String app, String variableName) {
        return map.get(Identifier.fromKeys(app, variableName));
    }

    /**
     * Check if the variable meta qualified by the app and name has already exist.
     *
     */
    public boolean containsVariableMeta(String app, String variableName) {
        return getVariableMeta(app, variableName) != null;
    }

    /**
     * Get variable meta identified by id.
     *
     */
    public VariableMeta getVariableMeta(Identifier id) {
        return map.get(id);
    }

    /**
     * Check if the variable meta qualified by the id has already exist.
     *
     */
    public boolean containsVariableMeta(Identifier id) {
        return getVariableMeta(id) != null;
    }

    /**
     * Get variable meta for one variable.
     *
     */
    public VariableMeta getVariableMeta(Variable v) {
        if (v == null) {
            throw new IllegalArgumentException("null variable");
        }

        return getVariableMeta(v.getApp(), v.getName());
    }

    /**
     * Check if the variable meta related to the variable has already exist.
     *
     */
    public boolean containsVariableMeta(Variable v) {
        return getVariableMeta(v) != null;
    }

    /**
     * Add variable meta to the registry.
     *
     * @param meta the meta which is to be added
     * @param override whether override the existing meta for the same variable
     *
     * @return true if add the type is added successfully
     */
    public synchronized boolean addVariableMeta(VariableMeta meta, boolean override) {
        if (meta == null) {
            throw new IllegalArgumentException("can't add null meta");
        }

        boolean result = false;
        Identifier key = Identifier.fromKeys(meta.getApp(), meta.getName());
        if (override) {
            map.put(key, meta);
            result = true;
        } else {
            result = map.putIfAbsent(key, meta) == null;
        }
        updateCounters();
        return result;
    }

    /**
     * Add variable meta and not override the existing one.
     *
     * @param meta
     * @return
     */
    public synchronized boolean addVariableMeta(VariableMeta meta) {
        boolean result = addVariableMeta(meta, false);
        updateCounters();
        return result;
    }

    /**
     * Remove variable meta from the registry.
     *
     * @return true if the variable meta is removed successfully.
     */
    public synchronized boolean removeVariableMeta(VariableMeta meta) {
        if (meta == null) {
            return true;
        }
        boolean result = removeVariableMeta(Identifier.fromKeys(meta.getApp(), meta.getName()));
        updateCounters();
        return result;
    }

    public synchronized boolean removeVariableMeta(Identifier id) {
        boolean result = map.remove(id) != null;
        updateCounters();
        return result;
    }

    /**
     * Get all the variable metas.
     *
     */
    public List<VariableMeta> getAllVariableMetas() {
        if (map.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(map.values());
    }

    /**
     * Clear the variable metas.
     *
     */
    public synchronized void clearVariableMetas() {
        map.clear();
        updateCounters();
    }

    /**
     * Update the VariableMetas
     * @param list
     */
    public synchronized void updateVariableMetas(List<VariableMeta> list) {
        ConcurrentMap<Identifier, VariableMeta> newMap = new ConcurrentHashMap<>();
        if (list != null) {
            for (VariableMeta v : list) {
                newMap.put(Identifier.fromKeys(v.getApp(), v.getName()), v);
            }
        }
        this.map = newMap;
        updateCounters();
    }

    /**
     * Get the number of the non-internal variables
     * @return
     */
    public int getNonInternalCount() {
        return this.nonInternalCount;
    }

    private synchronized void updateCounters() {
        int count = 0;
        for(VariableMeta v : map.values())
            if (!v.isInternal()) {
                count++;
            }

        this.nonInternalCount = count;
    }
}
