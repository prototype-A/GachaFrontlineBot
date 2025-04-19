package com.prototypeA.discordbot.GachaFrontlineBot;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


/**
 * The base of all classes that store a list of 
 * default settings.
 */
public abstract class AbstractHasDefaultSettings {
    
    protected final List<Setting> DEFAULT_SETTINGS;

    /**
     * Constructs a new instance of the class with an 
     * unmodifiable list of default settings.
     * 
     * @param name The name of the class.
     * @param desc The description of the class.
     * @param defaultSettings The default settings of the class.
     * @param cannotDisable Whether or not the class' functionality 
     * can be disabled.
     */
    protected AbstractHasDefaultSettings(
            String name,
            String desc,
            List<Setting> defaultSettings,
            boolean cannotDisable) {
        DEFAULT_SETTINGS = Collections.unmodifiableList(Stream.concat(
            cannotDisable
                ? Stream.of()
                : Stream.of(new Setting(
                    String.format("Enable %s", name),
                    String.format(
                        "%s\n",
                        desc,
                        name.toLowerCase()
                            .substring(0, name.indexOf(":"))
                    ),
                    "True",
                    Setting.Type.Boolean
                )),
            Stream.of(defaultSettings.toArray(Setting[]::new))
        ).toList());
    }


    /**
     * Returns the list of settings with their default values.
     * 
     * @return The list of default settings of this class.
     */
    public List<Setting> getDefaultSettings() {
        return DEFAULT_SETTINGS;
    }
}
