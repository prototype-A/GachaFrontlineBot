package com.prototypeA.discordbot.GachaFrontlineBot.tasks;

import com.prototypeA.discordbot.GachaFrontlineBot.ServerSettingsRepository;
import com.prototypeA.discordbot.GachaFrontlineBot.Setting;
import com.prototypeA.discordbot.GachaFrontlineBot.handlers.AbstractSlashCommandHandler;
import com.prototypeA.discordbot.GachaFrontlineBot.handlers.AbstractTaskHandler;
import com.prototypeA.discordbot.GachaFrontlineBot.permissions.ServerPermissions;
import com.prototypeA.discordbot.GachaFrontlineBot.util.MessageUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;


/**
 * Task that allows slash commands to be invoked 
 * through messages with a different prefix.
 */
@Component
@PropertySources({
    @PropertySource("file:bot.properties"),
    @PropertySource(value = "file:${spring.profiles.active}-bot.properties", ignoreResourceNotFound = true)
})
public final class MessageCommandToSlashCommandTask extends AbstractTaskHandler<MessageCreateEvent> {

    private final Map<String, AbstractSlashCommandHandler> COMMAND_LIST;

    @Autowired @Lazy
    protected ServerSettingsRepository serverSettings;

    /**
     * Constructs a new task handler that invokes commands 
     * through sent messages.
     * 
     * @param commandList The list of this application's 
     * slash commands.
     * @param prefix The character to prefix to a command 
     * sent through a message.
     */
    public MessageCommandToSlashCommandTask(
            List<AbstractSlashCommandHandler> commandList,
            @Value("${prefix:.}") String prefix) {
        super(
            "Message Commands",
            "Allows invoking application commands through messages " + 
                "with a specified prefix.",
            List.of(
                new Setting(
                    "Message Command Prefix",
                    "The character to prepend to a command in order to invoke it through a message.",
                    prefix,
                    Setting.Type.String
                )
            )
        );

        // Map commands by name
        COMMAND_LIST = Collections.unmodifiableMap(commandList.stream()
            .collect(Collectors.toMap(
                AbstractSlashCommandHandler::getCommandName,
                Function.identity()
            )));
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
     * @param prefix The string prefixed to the command message 
     * to remove.
     * @return The name of the command invoked by the message.
     */
    private String getInvokedCommand(MessageCreateEvent event, String prefix) {
        return MessageUtils.getMessageContent(event)
            .substring(prefix.length())
            .split(" ")[0]
            .toLowerCase();
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
    private String getServerPrefix(MessageCreateEvent event) {
        return serverSettings.getServerSettings(event.getGuildId()
            .get()
            .asLong())
            .get("Message Command Prefix")
            .getValue();
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        return MessageUtils.filterMessages(event)
            .flatMap(this::runCommand);
    }

    /**
     * Runs the message version of the invoked command.
     * 
     * @param event The event containing the message sent.
     * @return An empty {@code Mono} upon completion of execution.
     */
    private Mono<Void> runCommand(MessageCreateEvent event) {
        String prefix = getServerPrefix(event);
        AbstractSlashCommandHandler invokedCommand = COMMAND_LIST.getOrDefault(
            getInvokedCommand(event, prefix),
            null
        );

        return Mono.just(event)
            // Invoked a command?
            .filter(message -> MessageUtils.getMessageContent(message)
                .startsWith(prefix) && invokedCommand != null)
            // Is command enabled for that server?
            .filter(message -> {
                try {
                    return Boolean.parseBoolean(serverSettings.getServerSettings(message.getGuildId()
                        .get()
                        .asLong())
                        .get("Enable Command: /" + invokedCommand.getCommandName())
                        .getValue());
                } catch (Exception e) {
                    // Command cannot be disabled
                    return true;
                }
            })
            // Check server permissions
            .filter(message -> {
                // Check server permissions if command was used in a server
                try {
                    ServerPermissions serverPermissions = permissions.of(message.getGuildId()
                        .get());
                    Member invoker = message.getMember()
                        .get();

                    if (invokedCommand.isAdminCommand()) {
                        return serverPermissions.canUseAdminFunctionality(invoker);
                    }
                    return serverPermissions.canUseApplication(invoker);
                } catch (Exception e) {
                    // Command invoked in DMs
                    return true;
                }
            })
            // Run slash command
            .flatMap(invokedCommand::run);
    }
}
