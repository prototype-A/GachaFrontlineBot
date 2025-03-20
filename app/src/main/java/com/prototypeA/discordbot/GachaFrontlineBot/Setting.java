package com.prototypeA.discordbot.GachaFrontlineBot;

import java.util.List;


/**
 * Class to store a representation of an 
 * immutable setting for a command.
 */
public class Setting {

    /**
     * The type of the setting's value
     */
    public enum Type {
        // A True/False value
        Boolean,
        // The ID of one or more Roles within a server
        Role,
        // A string text value
        String,
        // The ID of one or more Users within a server
        User
    };

    private final String NAME;
    private final String DESC;
    private final String VALUE;
    private final Type TYPE;
    
    /**
     * Constructs a new setting with the provided default values.
     * 
     * @param name The display name of the setting.
     * @param desc The description of the setting.
     * @param value The default value of the setting.
     * @param type The type of this setting's value.
     */
    public Setting(String name, String desc, String value, Type type) {
        NAME = name;
        DESC = desc;
        VALUE = value;
        TYPE = type;
    }


    /**
     * Returns the description of this setting.
     * 
     * @return The description of this setting.
     */
    public String getDescription() {
        return DESC;
    }

    /**
     * Returns the name of this setting.
     * 
     * @return The name of this setting.
     */
    public String getName() {
        return NAME;
    }

    /**
     * Returns the type enum of this setting's value 
     * (Boolean, Role, or User).
     * 
     * @return The enum of this setting's value type.
     */
    public Type getType() {
        return TYPE;
    }

    /**
     * Returns the default value of this setting.
     * 
     * @return The default value of this setting.
     */
    public String getValue() {
        return VALUE;
    }

    /**
     * Returns the default value(s) of this setting as a list.
     * 
     * @return The default value(s) of this setting as a list.
     */
    public List<String> getValues() {
        return VALUE.equals("")
            ? List.of()
            : List.of(VALUE.split(", "));
    }
}
