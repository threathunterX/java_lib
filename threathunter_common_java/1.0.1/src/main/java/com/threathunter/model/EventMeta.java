package com.threathunter.model;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;

import java.util.List;
import java.util.Map;

/**
 * Meta information for the special kind of event.
 *
 * created by www.threathunter.cn
 */
public interface EventMeta {
    /**
     * The application where the event is used.
     *
     * The variable system may be used for different applications. <em>App</em>
     * acts as the namespace for both the <em>Event</em> and the <em>Variable</em>.
     *
     * <p>There is a special value "__all__" which means the event can be
     * used in all the applications.
     *
     * @return the application this event is used
     */
    public String getApp();

    /**
     * The event name which can differentiate it from other events in this
     * application.
     *
     * @return event name
     */
    public String getName();

    /**
     * The type that this event belongs to.
     *
     * Events can be grouped into different types. The events with same type may have
     * some characteristics in common.
     *
     * @return
     */
    public String getType();

    /**
     * Whether the event is defined manually directly or derived in the internal system.
     *
     * @return false if the event is manually defined and from the outside world directly,
     *         true if the event is derived by the internal system and will be
     *         emitted to the outside world.
     */
    public boolean isDerived();

    /**
     * Get the identifier of the variable where the event comes from.
     *
     * If the {code isDerived()} is true, which means the event is generated from some
     * variable, this method will give the parent variable identifier. If it is not a derived
     * event, this method will simply return null.
     *
     * We use identifer to decouple the event and variable.
     *
     * @return the source variable identifer if this is a derived event; null if there is not one.
     */
    public Identifier getSrcVariableID();

    /**
     * Get the variable meta where the event comes from.
     *
     * Usually each event meta will only contain the source variable identifier, the source
     * variable meta can be found indirectly from the meta registry.
     */
    public VariableMeta getSrcVariable();

    /**
     * The properties attached to the event.
     *
     * You may attach many additional properties to the event.
     * The property name can't be app/name/key/timestamp/value/
     * @return
     */
    public List<Property> getProperties();

    /**
     * Check if an property exists.
     *
     * @return true if the property exists.
     */
    public boolean hasProperty(Property property);

    /**
     * The schema for the data contained in the event.
     *
     * <p>The event should contain lots of information, and we need to give the schema
     *
     * <p>The schema should contain:
     * <ul>
     *     <li>app       |  {@code NamedType.STRING}  | {@code Event.getApp()}</li>
     *     <li>name      |  {@code NamedType.STRING}  | {@code Event.getName()}</li>
     *     <li>key       |  {@code NamedType.STRING}  | {@code Event.getKey()}</li>
     *     <li>timestamp |  {@code NamedType.DOUBLE}  | {@code Event.getTimestamp()}</li>
     *     <li>value     |  {@code NamedType.DOUBLE}  | {@code Event.getValue()}</li>
     *     <li>others    |  including data instances of {@code EventMeta.getProperties()}</li>
     * </ul>
     *
     * <p>The first five items are predefined, while the others can be defined by users.
     *
     * @return the schema about the event.
     */
    public Map<String, NamedType> getDataSchema();

    /**
     * When this event should expire and be deactivated.
     *
     * Some events will exist for ever, while others are generated temporarily and
     * should expire in the near future.
     *
     * @return the time when the event should expire, -1 means this event
     * will never expire.
     */
    public long expireAt();

    /**
     * Register in the registry.
     */
    public void active();

    /**
     * Unregister in the registry.
     */
    public void deactive();

    /**
     * Description on the event
     * @return
     */
    public String getRemark();
}
