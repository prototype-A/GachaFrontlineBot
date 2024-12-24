package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;

import reactor.core.publisher.Mono;


/**
 * Invokes the corresponding slash command from one sent through a message 
 * to be handled by a single command handler.
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
            .filter(message -> {
                Optional<User> user = message.getMessage()
                    .getAuthor();
                return user.isPresent() && !user.get()
                    .isBot();
            })
            // Ignore slash command messages (also emits MessageCreateEvent)
            .filter(message -> !message.getMessage()
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
