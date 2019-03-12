package com.threathunter.model;

import com.threathunter.common.ObjectId;
import com.threathunter.common.Utility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contents of an event should have.
 *
 * <p>Event will be the carrier of data transfering in the whole system.
 *
 * <ul>
 *     <li>{@code app} and {@code name} will identify the kind of this event.</li>
 *     <li>{@code key}, {@code id} and {@code timestamp} will identify the special instance
 *     this event is about.</li>
 *     <li>h{@code pid} is the id of the parent event</li>
 *     <li>{@code value} is the value of the event, it this is not necessary, you can just assign 1.0 to it</li>
 *     <li>{@code data} contains more detailed data.</li>
 * </ul>
 *
 * created by www.threathunter.cn
 */
public class Event {
    private final Map<String, Object> EMPTYPROPERTIES =
            Collections.unmodifiableMap(new HashMap<String, Object>());

    private String app;
    private String name;
    private String key;
    private long timestamp;
    private String id;
    private String pid;
    private double value;
    private Map<String, Object> propertyValues = EMPTYPROPERTIES;

    public Event() {
        // mainly for json
    }

    public Event(String app, String name, String key, long timestamp, String id, String pid, Double value,
                 Map<String, Object> propertyValues) {
        setApp(app);
        setName(name);
        setKey(key);
        setValue(value);
        setTimestamp(timestamp);
        setPropertyValues(propertyValues);
        setId(id);
        setPid(pid);
    }

    // for legacy constuctor
    public Event(String app, String name, String key, long timestamp, Double value,
                 Map<String, Object> propertyValues) {
        this(app, name, key, timestamp, null, null, value, propertyValues);
    }

    public Event(String app, String name, String key) {
        this(app, name, key, System.currentTimeMillis(), null, null, 1.0, new HashMap<String, Object>());
    }

    public void setApp(String app) {
        Utility.argumentNotEmpty(app, "app is null or empty");
        this.app = app;
    }

    public void setName(String name) {
        Utility.argumentNotEmpty(name, "name is null or empty");
        this.name = name;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setId(String id) {
        if (id != null && !id.isEmpty()) {
            if (!ObjectId.isValid(id)) {
                throw new RuntimeException("invalid event id");
            }
            this.id = id;
        }
    }

    public void setPid(String pid) {
        if (pid != null && !pid.isEmpty()) {
            if (!ObjectId.isValid(pid)) {
                throw new RuntimeException("invalid event pid");
            }
            this.pid = pid;
        }
    }

    public void setPropertyValues(Map<String, Object> propertyValues) {
        Utility.argumentNotEmpty(propertyValues, "propertyValues is null or empty");

        // we only use long/double, so convert the uncorrect values;
        Map<String, Object> newPropertyValues = new HashMap<>();
        for(String key : propertyValues.keySet()) {
            Object value = propertyValues.get(key);
            if (value instanceof Integer) {
                value = ((Integer)value).longValue();
            }
            if (value instanceof Short) {
                value = ((Short)value).longValue();
            }
            if (value instanceof Float) {
                value = ((Float)value).doubleValue();
            }
            newPropertyValues.put(key, value);
        }
        this.propertyValues = newPropertyValues;
    }

    /**
     * The application where the event is used.
     *
     * Events may be used for different applications. <em>App</em> acts as the namespace.
     *
     * <p>There is a special value "__all__" which means the event can be
     * used in all the applications.
     *
     * @return the application where this event is used
     */
    public String getApp() {
        return app;
    }

    /**
     * The event name which can differentiate it from other events in this
     * application.
     *
     * @return event name
     */
    public String getName() {
        return name;
    }

    /**
     * The key of the Event.
     *
     * Events contain data for different entities, but there should be one
     * fixed entity for each event. key is used as identifier for the related
     * entity, and it can also be used as sharding factor in distributed system.
     *
     * @return the identifier of the entity that this event is related to
     */
    public String getKey() {
        return key;
    }

    /**
     * The value of the Event.
     *
     * Events may contain a double value. you can assign arbitrary value to it if
     * you don't need it.
     *
     * @return the value of the event
     */
    public double value() {
        return value;
    }

    /**
     * The time when the event occurs
     *
     * @return timestamp of the event
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * The ID of the event
     *
     * we use mongodb style timestamp id, so it's a 24-byte long string
     */
    public String getId() {
        if (id == null || id.isEmpty()) {
            id = ObjectId.get().toHexString();
        }
        return id;
    }

    /**
     * The ID of the parent event
     *
     * we use mongodb style timestamp id, so it's a 24-byte long string
     */
    public String getPid() {
        if (pid == null || pid.isEmpty()) {
            pid = ObjectId.ZEROID.toHexString();
        }
        return pid;
    }

    /**
     * Additional data attached to the event.
     *
     */
    public Map<String, Object> getPropertyValues() {
        return propertyValues;
    }

    /**
     * All data of the event, including the property values and other values can be got
     * by the getters.
     *
     * All the data in the event can be described as a {@code Map<String, Object>},
     * including the fixing headers(like name, key) which already has getters to access,
     * and the properties that are attached to the event
     *
     */
    public Map<String, Object> genAllData() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.putAll(propertyValues);
        result.put("app", app);
        result.put("name", name);
        result.put("key", key);
        result.put("timestamp", timestamp);
        result.put("value", value);
        result.put("id", getId());
        result.put("pid", getPid());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (timestamp != event.timestamp) return false;
        if (!getId().equals(event.getId())) return false;
        if (!app.equals(event.app)) return false;
        if (!name.equals(event.name)) return false;
        if (!key.equals(event.key)) return false;
        if (value != event.value) return false;
        return propertyValues.equals(event.propertyValues);

    }

    @Override
    public int hashCode() {
        int result = app.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + getId().hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + propertyValues.hashCode();
        result = 31 * result + new Double(value).hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "app='" + app + '\'' +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", id='" + getId()+ '\'' +
                ", pid='" + getPid() + '\'' +
                ", value=" + value+ '\'' +
                ", timestamp=" + timestamp +
                ", propertyValues=" + propertyValues +
                '}';
    }
}
