package com.threathunter.model;

import com.threathunter.common.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Single registry for event meta.
 *
 * created by www.threathunter.cn
 */
public class EventMetaRegistry {
    private volatile ConcurrentMap<Identifier, EventMeta> map = new ConcurrentHashMap<>();

    private static final EventMetaRegistry instance = new EventMetaRegistry();

    private EventMetaRegistry(){}

    public static EventMetaRegistry getInstance() {
        return instance;
    }

    /**
     * Get event meta identified by the app and event name.
     *
     */
    public EventMeta getEventMeta(String app, String eventName) {
        return map.get(Identifier.fromKeys(app, eventName));
    }

    /**
     * Check if the event meta qualified by the app and name has already exist.
     *
     */
    public boolean containsEventMeta(String app, String eventName) {
        return getEventMeta(app, eventName) != null;
    }

    /**
     * Get event meta identified by the identifier.
     *
     */
    public EventMeta getEventMeta(Identifier id) {
        return map.get(id);
    }

    /**
     * Check if the event meta qualified by the id has already exist.
     *
     */
    public boolean containsEventMeta(Identifier id) {
        return getEventMeta(id) != null;
    }

    /**
     * Get event meta for one event.
     *
     */
    public EventMeta getEventMeta(Event e) {
        if (e == null) {
            throw new IllegalArgumentException("null event");
        }

        return getEventMeta(e.getApp(), e.getName());
    }

    /**
     * Check if the event meta related to this event has already exist.
     *
     */
    public boolean containsEventMeta(Event e) {
        return getEventMeta(e) != null;
    }

    /**
     * Add event meta to the registry.
     *
     * @param meta the meta which is to be added
     * @param override whether override the existing meta for the same variable
     *
     * @return true if add the type is added successfully
     */
    public boolean addEventMeta(EventMeta meta, boolean override) {
        if (meta == null) {
            throw new IllegalArgumentException("can't add null meta");
        }

        Identifier key = Identifier.fromKeys(meta.getApp(), meta.getName());
        if (override) {
            map.put(key, meta);
            return true;
        } else {
            return map.putIfAbsent(key, meta) == null;
        }
    }

    /**
     * Add event meta and not override the existing one.
     *
     * @param meta
     * @return
     */
    public boolean addEventMeta(EventMeta meta) {
        return addEventMeta(meta, false);
    }

    /**
     * Remove the event meta from the registry.o
     *
     * @param meta
     * @return true if the event meta is removed successfully.
     */
    public boolean removeEventMeta(EventMeta meta) {
        if (meta == null) {
            return true;
        }

        return removeEventMeta(Identifier.fromKeys(meta.getApp(), meta.getName()));
    }

    public boolean removeEventMeta(Identifier id) {
        return map.remove(id) != null;
    }

    /**
     * Get all the event metas.
     *
     */
    public List<EventMeta> getAllEventMetas() {
        if (map.isEmpty()) {
            return new ArrayList<EventMeta>();
        }

        return new ArrayList<EventMeta>(map.values());
    }

    /**
     * Clear the event metas.
     *
     */
    public void clearEventMetas() {
        map.clear();
    }

    public void updateEventMetas(List<EventMeta> list) {
        ConcurrentMap<Identifier, EventMeta> newMap = new ConcurrentHashMap<>();
        if (list != null) {
            for(EventMeta e : list) {
                newMap.put(Identifier.fromKeys(e.getApp(), e.getName()), e);
            }
        }
        this.map = newMap;
    }
}
