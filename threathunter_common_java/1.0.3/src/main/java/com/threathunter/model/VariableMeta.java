package com.threathunter.model;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.common.Utility;
import com.threathunter.config.CommonDynamicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Meta information for the special kind of variable.
 *
 * @author Wen Lu
 */
public abstract class VariableMeta {
    private static final Map<String, Class<? extends VariableMeta>> registry = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(VariableMeta.class);

    public static void init() {
        String[] packages = CommonDynamicConfig.getInstance().getStringArray("variable.meta.packages");
        if (packages == null || packages.length <= 0) {
            packages = new String[] { "com.threathunter" };
        }
        for(String p : packages) {
            Set<Class<? extends VariableMeta>> classes = Utility.scannerSubTypeFromPackage(p, VariableMeta.class);
            for(Class cls : classes) {
                try {
                    int m = cls.getModifiers();
                    if (Modifier.isAbstract(m) || Modifier.isInterface(m)) {
                        continue;
                    }

                    Field f = cls.getDeclaredField("TYPE");
                    String type = (String) f.get(null);
                    addSubClass(type, cls);
                } catch (Exception ex) {
                    logger.error("fatal:init:fail to process class:" + cls.getName(), ex);
                }
            }
        }
    }

    public static void addSubClass(String type, Class<? extends VariableMeta> cls) {
        registry.put(type, cls);
    }

    public static Class<? extends VariableMeta> findSubClass(String type) {
        return registry.get(type);
    }

   public abstract Identifier getId();

    /**
     * The application where the variable is used.
     *
     * The variable system may be used for different applications. <em>App</em>
     * acts as the namespace for both the <em>Event</em> and the <em>Variable</em>.
     *
     * <p>There is a special value "__all__" which means the variable can be
     * used in all the applications.
     *
     * @return the application this variable is used
     */
    public abstract String getApp();

    /**
     * The variable name which can differentiate it from other variables in this
     * application.
     *
     * @return variable name
     */
    public abstract String getName();

    /**
     * The type that this variable belongs to.
     *
     * Variables can be grouped into different types. The variables with same type may have
     * some characteristics in common.
     *
     * @return
     */
    public abstract String getType();

    public abstract String getStatus();
    /**
     * Whether the variable data comes from other variables.
     *
     * <p>There are two kinds of variables:
     * <ul>
     *     <li>Non-derived: the data is from the event directly</li>
     *     <li>derived: the data is from other variables</li>
     * </ul>
     * Actually, all the event should be first converted to non-derived variables,
     * and then be used further, this can simplify the whole system.
     *
     * @return false if the variable data is from some event
     *         true if the variable is generated on the basis of other variables.
     */
    public abstract boolean isDerived();

    /**
     * Return the source variables identities if it is a derived variable.
     */
    public abstract List<Identifier> getSrcVariableMetasID();

    /**
     * Return the source event identifier if it is a non-derived variable.
     */
    public abstract Identifier getSrcEventMetaID();

    /**
     * Whether the variable is auxiliary and defined/used internally.
     *
     * The variables can also be classified in this style:
     * <ul>
     *     <li>non-internal variable: this kind of variable is defined and used by the
     *     admin/users directly</li>
     *     <li>internal variable: this kind of variable is internally defined and used,
     *     it can help calculate other variables, or be used for other usage.</li>
     * </ul>
     *
     * @return true if it is an internal variable.
     */
    public abstract boolean isInternal();

    /**
     * The priority of each variable.
     *
     * The priority may determine the executing order of different kinds of variables.
     * <ul>
     *     <li>Each variable have its own priority</li>
     *     <li>0 is the highest priority</li>
     *     <li>Variable has lower priority than all of its source variables</li>
     * </ul>
     *
     * @return
     */
    public abstract int getPriority();

    /**
     * The non-fixed properties attached to the variable.
     *
     * You may attach many additional properties to the variable.
     * The property name can't be app/name/key/timestamp/value/
     * @return
     */
    public abstract List<Property> getProperties();

    /**
     * Check if an property exists.
     *
     * @return true if the property exists.
     */
    public abstract boolean hasProperty(Property property);

    /**
     * The schema for the data contained in the variable.
     *
     * <p>The variable should contain lots of information, and we need to give the schema
     *
     * <p>The schema should contain:
     * <ul>
     *     <li>app       |  {@code NamedType.STRING}  | {@code Variable.getApp()}</li>
     *     <li>name      |  {@code NamedType.STRING}  | {@code variable.getName()}</li>
     *     <li>key       |  {@code NamedType.STRING}  | {@code Variable.getKey()}</li>
     *     <li>timestamp |  {@code NamedType.DOUBLE}  | {@code Variable.getTimestamp()}</li>
     *     <li>value     |  {@code NamedType.OBJECT}  | {@code Variable.getValue()}</li>
     *     <li>others    |  including data instances of {@code VariableMeta.getProperties()}</li>
     * </ul>
     *
     * <p>The first five items are predefined, while the others can be defined by users.
     *
     * @return the schema about the variable.
     */
    public abstract Map<String, NamedType> getDataSchema();

    /**
     * When this variable meta should expire and be deactivated.
     *
     * Some variable meta will run for ever, while others are generated temporarily and
     * should expire in the near future.<br/>
     * This is different from the ttl, as this value is for the variable meta, while the
     * ttl is for the specific variable instance.
     *
     * @return the time when the variable should expire, 0 means this variable
     * will never expire.
     */
    public abstract long getExpireDate();

    /**
     * Time to live for a single variable instance.
     *
     * Each instance of variable may only have a short time to live, so that the resources
     * can be free. The underlying implementations should guarantee the variable instance
     * is alive for this given period, they can make their own decisions about whether to
     * destroy the instance when the ttl is passed.
     *
     * @return the time to live for the variable instance. 0 means for ever.
     */
    public abstract long getTtl();
    
    /**
     * Get one property from the variablemeta by the property name.
     * @param name property Name
     * @return propety with the given name
     */
    public abstract Property findPropertyByName(String name);

    /**
     * Get property mapping which convert properties of src variables into new
     * properties of this variable.
     *
     * @return all the mappings
     */
    public abstract List<PropertyMapping> getPropertyMappings();

    /**
     * Get condition that help determine whether generate a new value for this
     * variable from the src variables.
     *
     * @return condition on the source variables
     */
    public abstract PropertyCondition getPropertyCondition();

    /**
     * Get property reductions that will do some aggregation function on the source
     * variables' properties and generate an synthetic value for this variable
     *
     * @return all the reductions on the source variables;
     */
    public abstract PropertyReduction getPropertyReduction();

    /**
     * Get the description of the variable.
     *
     * @return
     */
    public abstract String getRemark();

    /**
     * Get plain object representation of this entity
     *
     * @return
     */
    public abstract String getVisibleName();

    /**
     * the key dimension of this variable
     *
     * @return
     */
    public abstract String getDimension();

    /**
     * sliding or slot
     *
     * @return
     */
    public abstract String getModule();

    /**
     * the type of return value
     *
     * @return
     */
    public abstract String getValueType();

    public abstract Object to_json_object();

    public abstract List<Property> getGroupKeys();
    /**
     * Build a specific variable meta according to the type
     * @param obj
     * @return
     */
    public static VariableMeta from_json_object(Object obj) {
        try {
            Map<Object, Object> mObj = (Map<Object, Object>)obj;
            String type = (String)mObj.get("type");
            Class cls = findSubClass(type);
            if (cls == null) {
                throw new IllegalStateException("unsupported type: " + type);
            }

            Method method = cls.getMethod("from_json_object", Object.class);
            return (VariableMeta)method.invoke(null, obj);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
