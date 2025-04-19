package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import com.prototypeA.discordbot.GachaFrontlineBot.AbstractHasDefaultSettings;
import com.prototypeA.discordbot.GachaFrontlineBot.ServerSettingsRepository;
import com.prototypeA.discordbot.GachaFrontlineBot.Setting;
import com.prototypeA.discordbot.GachaFrontlineBot.permissions.Permissions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;

import reactor.core.publisher.Mono;


/**
 * The base of all extending handlers tasked with 
 * running commands invoked by users.
 */
public abstract class AbstractCommandHandler<T extends Event> extends AbstractHasDefaultSettings implements IEventHandler<T> {
    
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected final String COMMAND_NAME;
    protected final String COMMAND_DESC;
    protected final boolean SINGLE_INSTANCE;
    protected String inUseMessage = "This command is currently in use.";
    private final Set<Snowflake> SERVERS_USING_COMMAND;
    protected final boolean IS_ADMIN_COMMAND;

    @Autowired
    protected Permissions permissions;
    @Autowired @Lazy
    protected ServerSettingsRepository serverSettings;

    /**
     * Constructs a generic command handler.
     * 
     * @param commandName The string that will invoke this command.
     * @param description The description of this command.
     */
    protected AbstractCommandHandler(String commandName, String description) {
        this(commandName, description, List.of());
    }

    /**
     * Constructs a generic command handler.
     * 
     * @param commandName The string that will invoke this command.
     * @param description The description of this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     */
    protected AbstractCommandHandler(
            String commandName,
            String description,
            List<Setting> defaultSettings) {
        this(commandName, description, defaultSettings, false);
    }

    /**
     * Constructs a generic command handler.
     * 
     * @param commandName The string that will invoke this command.
     * @param description The description of this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     * @param onlyAllowOneInstance Prevents others in the same server 
     * from invoking this command when it is currently in use.
     */
    protected AbstractCommandHandler(
            String commandName,
            String description,
            List<Setting> defaultSettings,
            boolean onlyAllowOneInstance) {
        this(
            commandName,
            description,
            defaultSettings,
            onlyAllowOneInstance,
            false
        );
    }

    /**
     * Constructs a generic command handler.
     * 
     * @param commandName The string that will invoke this command.
     * @param description The description of this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     * @param onlyAllowOneInstance Prevents others in the same server 
     * from invoking this command when it is currently in use.
     * @param isAdminCommand Whether or not the invoker requires admin 
     * permissions to invoke this command.
     */
    protected AbstractCommandHandler(
            String commandName,
            String description,
            List<Setting> defaultSettings,
            boolean onlyAllowOneInstance,
            boolean isAdminCommand) {
        this(
            commandName,
            description,
            defaultSettings,
            onlyAllowOneInstance,
            isAdminCommand,
            false
        );
    }

    /**
     * Constructs a generic command handler.
     * 
     * @param commandName The string that will invoke this command.
     * @param description The description of this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     * @param onlyAllowOneInstance Prevents others in the same server 
     * from invoking this command when it is currently in use.
     * @param isAdminCommand Whether or not the invoker requires admin 
     * permissions to invoke this command.
     * @param cannotDisable Whether or not this command can be disabled.
     */
    protected AbstractCommandHandler(
            String commandName,
            String description,
            List<Setting> defaultSettings,
            boolean onlyAllowOneInstance,
            boolean isAdminCommand,
            boolean cannotDisable) {
        super(
            "Command: /" + commandName.toLowerCase(),
            description,
            defaultSettings,
            cannotDisable
        );

        COMMAND_NAME = commandName.toLowerCase();
        COMMAND_DESC = description;
        SINGLE_INSTANCE = onlyAllowOneInstance;
        SERVERS_USING_COMMAND = new HashSet<>();
        IS_ADMIN_COMMAND = isAdminCommand;
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
     * Returns whether this command requires elevated permissions to invoke.
     * 
     * @return True if this command requires elevated permissions to invoke.
     */
    public boolean isAdminCommand() {
        return IS_ADMIN_COMMAND;
    }

    /**
     * Returns whether the specified user is the same user that invoked 
     * the command (i.e. the author of the message that this command 
     * replied to).
     * 
     * @param event The emitted event containing the invoking user.
     * @param userId The ID of the user to compare to.
     * @return True if the user in the emitted event is the same user that 
     * invoked the command.
     */
    protected abstract boolean isInvokingUser(Event event, Snowflake userId);

    /**
     * Adds the specified server to the set of servers that this 
     * command is currently running in, preventing other users in 
     * those same servers from invoking this command while it is 
     * still running.
     * 
     * @param serverId The ID of the server that this command 
     * is currently running in.
     * @return True if this command was successfully locked 
     * for the specified server.
     */
    protected boolean lockCommandForServer(Snowflake serverId) {
        if (serverId == null || !SINGLE_INSTANCE) {
            return false;
        }

        return SERVERS_USING_COMMAND.add(serverId);
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

    /**
     * Removes the specified server from the set of servers 
     * that this command is currently running in, allowing 
     * other users in those servers to invoke this command.
     * 
     * @param serverId The ID of the server that this command 
     * is currently running in.
     * @return True if this command was previously locked for 
     * the specified server and was successfully unlocked.
     */
    protected boolean unlockCommandForServer(Snowflake serverId) {
        return SERVERS_USING_COMMAND.remove(serverId);
    }
}
