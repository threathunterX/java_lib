package com.threathunter.model;

import java.util.Map;

/**
 * Contents of an variable instance should have.
 *
 * <p>Variable will be the carrier of data inside the calculating system.
 *
 * <ul>
 *     <li>{@code app} and {@code name} will identify the kind of this variable.</li>
 *     <li>{@code key} and {@code timestamp} will identify the special instance
 *     this variable is about.</li>
 *     <li>{@code value} and {@code data} contains more detailed data.</li>
 * </ul>
 *
 * created by www.threathunter.cn
 */
public interface Variable {

    /**
     * The application where the variable is used.
     *
     * The variable system may be used for different applications. <em>App</em>
     * acts as the namespace for both the <em>Event</em> and the <em>Variable</em>.
     *
     * <p>There is a special value "__all__" which means the variable can be
     * used in all the applications.
     *
     * @return the application where this variable is used
     */
    public String getApp();

    /**
     * The variable name which can differentiate it from other variables in this
     * application.
     *
     * @return variable name
     */
    public String getName();

    /**
     * The time when the variable in the current state
     *
     * @return timestamp of the event
     */
    public long getTimestamp();

    /**
     * The key of the Variable.
     *
     * Variables contain data for different entities, but there should be one
     * fixed entity for each variable instance. key is used as identifier for the related
     * entity instance.
     *
     * @return the identifier of the entity that this variable instance is related to
     */
    public String getKey();

    /**
     * The data that the variable has.
     *
     * The data is calculated on the basis of the events or other variables, and will
     * be used by calculating other variables or generating events that user will be
     * interested.
     *
     * @return the data of the variable
     */
    public Object getValue();

    /**
     * The map version of this variable instance. All data of the variable,
     * including the properties and other values can be got by the previous
     * getters, will be populated in a map.
     *
     * All the data in the variable can be described as a {@code Map<String, Object>},
     * including the fixing headers(like name, key) which already has getters to access,
     * and the properties that are attached to the variable.
     *
     */
    public Map<String, Object> getData();

    /**
     * Information about this variable.
     *
     * Could be retrieved from the central variable meta registry.
     *
     * @return the meta data of this kind of variable.
     */
    public VariableMeta getMeta();
}
