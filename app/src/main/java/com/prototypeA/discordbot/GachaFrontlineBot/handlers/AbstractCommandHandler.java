package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import com.prototypeA.discordbot.GachaFrontlineBot.Setting;
import com.prototypeA.discordbot.GachaFrontlineBot.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import discord4j.core.event.domain.Event;

import reactor.core.publisher.Mono;


/**
 * The base of all extending handlers tasked with 
 * running commands invoked by users.
 */
public abstract class AbstractCommandHandler<T extends Event> implements IEventHandler<T> {
    
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected final String COMMAND_NAME;
    protected final boolean IS_ADMIN_COMMAND;
    protected final List<Setting> DEFAULT_SETTINGS;

    @Autowired
    protected Permissions permissions;

    /**
     * Constructs a generic command handler with the specified name of 
     * the command it will handle.
     * 
     * @param commandName The string that will invoke this command.
     */
    protected AbstractCommandHandler(String commandName) {
        this(commandName, List.of());
    }

    /**
     * Constructs a generic command handler with the specified name of 
     * the command it will handle along with its default settings.
     * 
     * @param commandName The string that will invoke this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     */
    protected AbstractCommandHandler(String commandName,
            List<Setting> defaultSettings) {
        this(commandName, defaultSettings, false);
    }

    /**
     * Constructs a generic command handler with the specified name of 
     * the command it will handle along with its default settings.
     * 
     * @param commandName The string that will invoke this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     * @param cannotDisable Whether or not this command can be disabled.
     */
    protected AbstractCommandHandler(String commandName,
            List<Setting> defaultSettings,
            boolean cannotDisable) {
        this(commandName, defaultSettings, cannotDisable, false);
    }

    /**
     * Constructs a generic command handler with the specified name of 
     * the command it will handle along with its default settings.
     * 
     * @param commandName The string that will invoke this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     * @param cannotDisable Whether or not this command can be disabled.
     * @param isAdminCommand Whether or not the invoker requires admin 
     * permissions to invoke this command.
     */
    protected AbstractCommandHandler(String commandName,
            List<Setting> defaultSettings,
            boolean cannotDisable,
            boolean isAdminCommand) {
        COMMAND_NAME = commandName;
        IS_ADMIN_COMMAND = isAdminCommand;

        // Store default settings
        DEFAULT_SETTINGS = new ArrayList<>();
        if (!cannotDisable) {
            DEFAULT_SETTINGS.add(
                new Setting(
                    String.format("Enable Command: /%s", COMMAND_NAME),
                    "Is this command able to be used in this server?",
                    "True",
                    Setting.Type.Boolean
                )
            );
        }
        defaultSettings.forEach(DEFAULT_SETTINGS::add);
    }


    /**
     * Returns the string that is used to invoke this command.
     * 
     * @return The name of this command.
     */
    public String getCommandName() {
        return COMMAND_NAME;
    }

    /**
     * Returns the list of settings with their default values 
     * for this command.
     * 
     * @return The list of this command's default settings.
     */
    public List<Setting> getDefaultSettings() {
        return DEFAULT_SETTINGS;
    }

    /**
     * The method that is executed when an error occurs 
     * during the handling of an event.
     * 
     * @param error The error thrown.
     * @return An empty {@code Mono} after handling the error.
     */
    @Override
    public Mono<Void> handleError(Throwable error) {
        LOG.error("Failed to handle command " + getName(), error);
        return Mono.empty();
    }

    /**
     * The method that contains the code to run when 
     * this command is invoked.
     * 
     * @param <T> The type of the emitted Discord event that this 
     * handler is tasked to handle.
     * @param event The event containing the command and its relevent data.
     * @return An empty {@code Mono} upon completion of execution.
     */
    protected abstract Mono<Void> run(T event);
}
