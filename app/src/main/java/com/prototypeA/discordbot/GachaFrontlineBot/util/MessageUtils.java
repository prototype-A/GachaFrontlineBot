package com.prototypeA.discordbot.GachaFrontlineBot.util;

import java.util.Optional;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.AllowedMentions;

import reactor.core.publisher.Mono;


/**
 * Utility class that contains commonly-used methods 
 * relating to Discord messages.
 */
public final class MessageUtils {

    /**
     * Disable instantiation.
     */
    private MessageUtils() {}


    /**
     * Removes the emitted {@code MessageCreateEvent} from the passed  
     * {@code Mono} parameter if it was sent by a bot application.
     * 
     * @param event The event emitted from a sent message.
     * @return The same passed {@code Mono} with the same emitted event 
     * if the message was not sent by a bot application.
     */
    public static Mono<MessageCreateEvent> filterBotMessages(Mono<MessageCreateEvent> event) {
        return event.filter(message -> {
            Optional<User> user = message.getMessage()
                .getAuthor();
            return user.isPresent() && !user.get()
                .isBot();
        });
    }

    /**
     * Removes the emitted {@code MessageCreateEvent} from the 
     * passed {@code Mono} parameter if it was an application's 
     * slash command (which also emits the event).
     * 
     * @param event The event emitted from a sent message.
     * @return The same passed {@code Mono} with the same emitted 
     * event if the message did not contain an interaction.
     */
    public static Mono<MessageCreateEvent> filterSlashCommands(Mono<MessageCreateEvent> event) {
        return event.filter(message -> !message.getMessage()
            .getInteraction()
            .isPresent());
    }

    /**
     * Returns the text content contained in the sent message.
     * 
     * @param event The event containing the message sent.
     * @return The string content of the message.
     */
    public static String getMessageContent(MessageCreateEvent event) {
        return event.getMessage()
            .getContent();
    }

    /**
     * Sends a message to the same channel as the interaction to reply to.
     * 
     * @param event The emitted event of the interaction to reply to.
     * @param content The text to send in the reply.
     * @param ephemeral True if the reply should only be seen by the 
     * interacting user.
     * @param pingInvoker True if the interaction user should be pinged 
     * when their message is replied to.
     * @return An empty {@code Mono} upon sending the reply.
     */
    public static Mono<Void> sendReply(
            ChatInputInteractionEvent event,
            String content,
            boolean ephemeral,
            boolean pingInvoker) {
        return event.reply(InteractionApplicationCommandCallbackSpec.builder()
            .content(content)
            .allowedMentions(AllowedMentions.builder()
                .repliedUser(pingInvoker)
                .build())
            .ephemeral(ephemeral)
            .build());
    }

    /**
     * Sends a message to the same channel as the message to reply to.
     * 
     * @param event The emitted event of the message to reply to.
     * @param content The text to send in the reply.
     * @param pingInvoker True if the user that sent the message 
     * should be pinged when their message is replied to.
     * @return An empty {@code Mono} upon sending the reply.
     */
    public static Mono<Void> sendReply(
            MessageCreateEvent event,
            String content,
            boolean pingInvoker) {
        return event.getMessage()
            .getChannel()
            .flatMap(channel -> channel.createMessage(MessageCreateSpec.builder()
                .content(content)
                .messageReference(event.getMessage()
                    .getId())
                .allowedMentions(AllowedMentions.builder()
                    .repliedUser(pingInvoker)
                    .build())
                .build())
                .then());
    }
}
