package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import com.prototypeA.discordbot.GachaFrontlineBot.ServerSettingsRepository;
import com.prototypeA.discordbot.GachaFrontlineBot.Setting;
import com.prototypeA.discordbot.GachaFrontlineBot.permissions.ServerPermissions;
import com.prototypeA.discordbot.GachaFrontlineBot.util.MessageUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;


/**
 * The base of extending handlers tasked with running commands 
 * invoked through sent messages.
 */
@PropertySources({
    @PropertySource("file:bot.properties"),
    @PropertySource(value = "file:${spring.profiles.active}-bot.properties", ignoreResourceNotFound = true)
})
public abstract class AbstractMessageCommandHandler extends AbstractCommandHandler<MessageCreateEvent> {

    @Autowired
    private ServerSettingsRepository serverSettings;

    /**
     * Constructs a message command handler with the specified name 
     * of the command it will handle, whether the message must be 
     * prefixed with a specific prefix (followed by its command name) 
     * to invoke the command, and its default settings.
     * 
     * @param commandName The string that will invokis the command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     */
    protected AbstractMessageCommandHandler(String commandName,
            List<Setting> defaultSettings) {
        this(commandName, defaultSettings, false, false);
    }

    /**
     * Constructs a message command handler with the specified name 
     * of the command it will handle, whether the message must be 
     * prefixed with a specific prefix (followed by its command name) 
     * to invoke the command, and its default settings.
     * 
     * @param commandName The string that will invoke this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     * @param cannotDisable Whether or not this command can be disabled.
     */
    protected AbstractMessageCommandHandler(String commandName,
            List<Setting> defaultSettings,
            boolean cannotDisable) {
        this(commandName, defaultSettings, cannotDisable, false);
    }

    /**
     * Constructs a message command handler with the specified name 
     * of the command it will handle, whether the message must be 
     * prefixed with a specific prefix (followed by its command name) 
     * to invoke the command, and its default settings.
     * 
     * @param commandName The string that will invoke this command.
     * @param defaultSettings The list of this command's available 
     * settings initialized with its default values.
     * @param cannotDisable Whether or not this command can be disabled.
     * @param isAdminCommand Whether or not the invoker requires admin 
     * permissions to invoke this command.
     */
    protected AbstractMessageCommandHandler(String commandName,
            List<Setting> defaultSettings,
            boolean cannotDisable,
            boolean isAdminCommand) {
        super(commandName, defaultSettings, cannotDisable, isAdminCommand);
    }


    @Override
    public Class<MessageCreateEvent> getEventClass() {
        return MessageCreateEvent.class;
    }

    /**
     * Attempts to remove the prefix from the start of the 
     * sent message and returns the first word delimited 
     * by a space.
     * 
     * @param event The event containing the message sent.
     * @param prefix The string prepended to the message to invoke commands.
     * @return The name of the command invoked by the message.
     */
    public String getInvokedCommand(MessageCreateEvent event, String prefix) {
        return MessageUtils.getMessageContent(event)
            .substring(prefix.length())
            .split(" ")[0]
            .toLowerCase();
    }

    /**
     * Returns the name of the command handler and its invoking string.
     * 
     * @return A formatted string containing the name of 
     * this event handler, the message command prefix, 
     * and the name of this command.
     */
    @Override
    public String getName() {
        return String.format(
            "%s (%s%s)",
            super.getName(),
            serverSettings.getDefaultMessageCommandPrefix(),
            this.COMMAND_NAME
        );
    }

    /**
     * Returns the server's message command prefix if the command 
     * was invoked in a server or the application's default message 
     * command prefix if it was invoked via direct message.
     * 
     * @param event The event containing the message sent.
     * @return The appropriate message command prefix depending on 
     * where the command was invoked.
     */
    protected String getPrefix(MessageCreateEvent event) {
        return serverSettings.getServerSettings(event.getGuildId()
            .get()
            .asLong())
            .get("Message Command Prefix")
            .getValue();
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        AtomicReference<String> prefix = new AtomicReference<>(serverSettings.getDefaultMessageCommandPrefix());

        return MessageUtils.filterSlashCommands(MessageUtils.filterBotMessages(Mono.just(event)))
            // Check server permissions
            .filter(command -> {
                // Check server permissions if command was used in a server
                try {
                    ServerPermissions serverPermissions = permissions.of(command.getMessage()
                        .getGuildId()
                        .get());
                    Member invoker = command.getMember()
                        .get();

                    // Get the command prefix for that server
                    prefix.set(getPrefix(command));

                    // Message starts with prefix
                    boolean startsWithPrefix = MessageUtils.getMessageContent(command)
                        .startsWith(prefix.get());

                    if (IS_ADMIN_COMMAND) {
                        return startsWithPrefix && serverPermissions.canUseAdminFunctionality(invoker);
                    }
                    return startsWithPrefix && serverPermissions.canUseApplication(invoker);
                } catch (Exception e) {}

                // Command invoked in DMs
                return true;
            })
            // Get invoked command
            .filter(command -> getInvokedCommand(command, prefix.get())
                .equalsIgnoreCase(COMMAND_NAME))
            // Run this command
            .flatMap(this::run);
    }
}
