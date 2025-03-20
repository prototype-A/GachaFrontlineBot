package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import com.prototypeA.discordbot.GachaFrontlineBot.Setting;
import com.prototypeA.discordbot.GachaFrontlineBot.permissions.ServerPermissions;

import java.util.List;

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
     * Constructs an application (slash) command handler with the specified 
     * name of the command it will handle.
     * 
     * @param commandName The string that will invoke this command.
     */
    protected AbstractSlashCommandHandler(String commandName) {
        this(commandName, List.of());
    }

    /**
     * Constructs an application (slash) command handler with the specified 
     * name of the command it will handle along with its default settings.
     * 
     * @param commandName The string that will invoke this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     */
    protected AbstractSlashCommandHandler(String commandName,
            List<Setting> defaultSettings) {
        this(commandName, defaultSettings, false);
    }

    /**
     * Constructs an application (slash) command handler with the specified 
     * name of the command it will handle along with its default settings.
     * 
     * @param commandName The string that will invoke this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     * @param cannotDisable Whether or not this command can be disabled.
     */
    protected AbstractSlashCommandHandler(String commandName,
            List<Setting> defaultSettings,
            boolean cannotDisable) {
        this(commandName, defaultSettings, cannotDisable, false);
    }

    /**
     * Constructs an application (slash) command handler with the specified 
     * name of the command it will handle along with its default settings.
     * 
     * @param commandName The string that will invoke this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     * @param cannotDisable Whether or not this command can be disabled.
     * @param isAdminCommand Whether or not the invoker requires admin 
     * permissions to invoke this command.
     */
    protected AbstractSlashCommandHandler(String commandName,
            List<Setting> defaultSettings,
            boolean cannotDisable,
            boolean isAdminCommand) {
        super(commandName, defaultSettings, cannotDisable, isAdminCommand);
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
            // Matches invoked command
            .filter(command -> command.getCommandName()
                .equalsIgnoreCase(COMMAND_NAME))
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

                    if (IS_ADMIN_COMMAND) {
                        return serverPermissions.canUseAdminFunctionality(invoker);
                    }
                    return serverPermissions.canUseApplication(invoker);
                } catch (Exception e) {}

                // Command invoked in DMs
                return true;
            })
            // Run invoked command
            .flatMap(this::run);
    }

    /**
     * The method that contains the code to run when 
     * the command is invoked via a sent message.
     * 
     * @param event The event containing the message sent.
     * @return An empty Mono upon completion of execution.
     */
    protected abstract Mono<Void> run(MessageCreateEvent event);
}
