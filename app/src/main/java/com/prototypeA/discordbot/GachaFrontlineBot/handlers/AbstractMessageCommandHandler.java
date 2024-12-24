package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;

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

    protected boolean isPrefixed;
    
    @Value("${prefix:.}")
    protected String prefix;

    protected AbstractMessageCommandHandler(String commandName, boolean isPrefixed) {
        super(commandName);
        this.isPrefixed = isPrefixed;
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
     * @param event The event containing the message sent
     * @return The name of the command invoked by the message
     */
    public String getInvokedCommand(MessageCreateEvent event) {
        return getMessageContent(event)
            .substring(isPrefixed ? prefix.length() : 0)
            .split(" ")[0]
            .toLowerCase();
    }

    /**
     * Returns the text content contained in the sent message.
     * 
     * @param event The event containing the message sent
     * @return The string content of the message
     */
    public String getMessageContent(MessageCreateEvent event) {
        return event.getMessage()
            .getContent();
    }

    @Override
    public String getName() {
        return String.format("%s (%s%s)", super.getName(), prefix, commandName);
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        return Mono.just(event)
            // Ignore bot requests
            .filter(message -> {
                Optional<User> user = message.getMessage()
                    .getAuthor();
                return user.isPresent() && !user.get()
                    .isBot();
            })
            // Ignore slash command messages (also emits MessageCreateEvent)
            .filter(interaction -> !interaction.getMessage()
                .getInteraction()
                .isPresent())
            // Message starts with prefix (if prefixed command)
            .filter(message -> !isCommand(message))
            // Run invoked command
            .filter(command -> getInvokedCommand(command)
                .equalsIgnoreCase(commandName))
            .flatMap(this::run);
    }

    /**
     * Returns whether the sent message starts with the specified 
     * command prefix.
     * 
     * @return True if the message starts with the prefix, otherwise false
     */
    public boolean isCommand(MessageCreateEvent event) {
        if (!isPrefixed) {
            return true;
        }
        return getMessageContent(event)
            .startsWith(prefix);
    }
}
