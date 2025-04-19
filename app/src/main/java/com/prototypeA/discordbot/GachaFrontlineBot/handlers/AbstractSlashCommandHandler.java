package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import com.prototypeA.discordbot.GachaFrontlineBot.Setting;
import com.prototypeA.discordbot.GachaFrontlineBot.permissions.ServerPermissions;

import java.util.List;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.discordjson.json.ApplicationCommandRequest;

import reactor.core.publisher.Mono;


/**
 * The base of extending handlers tasked with running commands 
 * invoked through slash ("/") interactions.
 */
public abstract class AbstractSlashCommandHandler extends AbstractCommandHandler<ChatInputInteractionEvent> {

    /**
     * Constructs a command handler that handles when the command 
     * is invoked through an application (slash) interaction.
     * 
     * @param commandName The string that will invoke this command.
     * @param description The description of this command.
     */
    protected AbstractSlashCommandHandler(String commandName, String description) {
        this(commandName, description, List.of());
    }

    /**
     * Constructs a command handler that handles when the command 
     * is invoked through an application (slash) interaction.
     * 
     * @param commandName The string that will invoke this command.
     * @param description The description of this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     */
    protected AbstractSlashCommandHandler(
            String commandName,
            String description,
            List<Setting> defaultSettings) {
        this(commandName, description, defaultSettings, false);
    }

    /**
     * Constructs a command handler that handles when the command 
     * is invoked through an application (slash) interaction.
     * 
     * @param commandName The string that will invoke this command.
     * @param description The description of this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     * @param onlyAllowOneInstance Prevents others in the same server 
     * from invoking this command when it is currently in use.
     */
    protected AbstractSlashCommandHandler(
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
     * Constructs a command handler that handles when the command 
     * is invoked through an application (slash) interaction.
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
    protected AbstractSlashCommandHandler(
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
     * Constructs a command handler that handles when the command 
     * is invoked through an application (slash) interaction.
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
    protected AbstractSlashCommandHandler(
            String commandName,
            String description,
            List<Setting> defaultSettings,
            boolean onlyAllowOneInstance,
            boolean isAdminCommand,
            boolean cannotDisable) {
        super(
            commandName,
            description,
            defaultSettings,
            onlyAllowOneInstance,
            isAdminCommand,
            cannotDisable
        );
    }


    /**
     * Returns the JSON representation of this command 
     * to be sent to Discord in order to register it as 
     * a guild/global slash command.
     * 
     * @return The JSON representation of this command.
     */
    public abstract ApplicationCommandRequest getCommandRequest();
    
    @Override
    public Class<ChatInputInteractionEvent> getEventClass() {
        return ChatInputInteractionEvent.class;
    }

    /**
     * Returns the name of the command handler and its invoking string.
     * 
     * @return A formatted string containing the name of 
     * this event handler along with its command name.
     */
    @Override
    public String getName() {
        return String.format("%s (/%s)", super.getName(), this.COMMAND_NAME);
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return Mono.just(event)
            // Ignore bot requests
            .filter(command -> !command.getInteraction()
                .getUser()
                .isBot())
            // Invoked this command?
            .filter(command -> command.getCommandName()
                .equalsIgnoreCase(COMMAND_NAME))
            // Is command enabled for that server?
            .filter(command -> {
                try {
                    return Boolean.parseBoolean(serverSettings.getServerSettings(command.getInteraction()
                        .getGuildId()
                        .get()
                        .asLong())
                        .get("Enable Command: /" + COMMAND_NAME)
                        .getValue());
                } catch (Exception e) {
                    // Command cannot be disabled
                    return true;
                }
            })
            // Check permissions
            .filter(command -> {
                // Check server permissions if command was used in a server
                try {
                    ServerPermissions serverPermissions = permissions.of(command.getInteraction()
                        .getGuildId()
                        .get());
                    Member invoker = command.getInteraction()
                        .getMember()
                        .get();

                    if (isAdminCommand()) {
                        return serverPermissions.canUseAdminFunctionality(invoker);
                    }
                    return serverPermissions.canUseApplication(invoker);
                } catch (Exception e) {
                    // Command invoked in DMs
                    return true;
                }
            })
            // Run this command
            .flatMap(this::run);
    }

    @Override
    protected boolean isInvokingUser(Event event, Snowflake userId) {
        if (event instanceof ChatInputInteractionEvent) {
            return ((ChatInputInteractionEvent) event).getInteraction()
                .getUser()
                .getId()
                .equals(userId);
        } else if (event instanceof MessageCreateEvent) {
            return ((MessageCreateEvent) event).getMessage()
                .getAuthor()
                .get()
                .getId()
                .equals(userId);
        }

        return false;
    }

    /**
     * The method that contains the code to run when 
     * the command is invoked via a sent message.
     * 
     * @param event The event containing the message sent.
     * @return An empty Mono upon completion of execution.
     */
    public abstract Mono<Void> run(MessageCreateEvent event);
}
