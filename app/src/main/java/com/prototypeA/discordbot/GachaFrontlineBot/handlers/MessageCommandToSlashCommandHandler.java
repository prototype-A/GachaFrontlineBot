package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import discord4j.core.event.domain.message.MessageCreateEvent;

import reactor.core.publisher.Mono;


/**
 * Invokes the corresponding slash command from ones sent via messages to be handled by 
 * a single slash command handler as if it was originally invoked as a slash command. 
 * This is to prevent having to create multiple classes for different (slash/message) 
 * events with only slight differences in their handle() methods but end up running the 
 * same code in the resulting run() method
 */
@Service
public final class MessageCommandToSlashCommandHandler extends AbstractMessageCommandHandler {

    private Map<String, AbstractSlashCommandHandler> commandList;

    public MessageCommandToSlashCommandHandler(List<AbstractSlashCommandHandler> commandList) {
        super("", true);
        this.commandList = commandList.stream()
            .collect(Collectors.toMap(AbstractSlashCommandHandler::getCommandName, Function.identity()));
    }


    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        return Mono.just(event)
            // Ignore bot requests
            .filter(message -> message.getMember()
                .isPresent() && !message.getMember()
                .get()
                .isBot())
            // Ignore slash command messages (also emits MessageCreateEvent)
            .filter(message -> message.getMessage()
                .getInteraction()
                .isPresent())
            // Message starts with prefix
            .filter(message -> isCommand(message))
            // Slash command exists
            .filter(message -> commandList.containsKey(getInvokedCommand(message)))
            // Create interaction from the message
            .flatMap(this::run);
    }

    @Override
    public Mono<Void> run(MessageCreateEvent event) {
        return commandList.get(getInvokedCommand(event))
            .run(event);
    }
}
