package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.prototypeA.discordbot.GachaFrontlineBot.util.MessageUtils;

import discord4j.core.event.domain.message.MessageCreateEvent;

import reactor.core.publisher.Mono;


/**
 * Invokes the corresponding message version of a slash command 
 * when called through a sent message.
 */
@Service
public final class MessageCommandToSlashCommandHandler extends AbstractMessageCommandHandler {

    private Map<String, AbstractSlashCommandHandler> commandList;

    /**
     * Constructs a new handler that scans sent messages for command invocations and runs the 
     * message version of that corresponding slash command.
     * 
     * @param commandList A list that contains every slash command handler in this application.
     */
    public MessageCommandToSlashCommandHandler(List<AbstractSlashCommandHandler> commandList) {
        super("", List.of(), true);
        this.commandList = commandList.stream()
            .collect(Collectors.toMap(AbstractSlashCommandHandler::getCommandName, Function.identity()));
    }


    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        return MessageUtils.filterSlashCommands(MessageUtils.filterBotMessages(Mono.just(event)))
            // Get slash command equivalent
            .filter(message -> commandList.containsKey(getInvokedCommand(message, getPrefix(message))))
            // Run slash command
            .flatMap(this::run);
    }

    /**
     * Invokes the method in the corresponding slash command handler 
     * which handles the message version of that command.
     * 
     * @param event The emitted event of the message invoking the command.
     * @return An empty mono upon completion of execution.
     */
    @Override
    protected Mono<Void> run(MessageCreateEvent event) {
        return commandList.get(getInvokedCommand(event, getPrefix(event)))
            .run(event);
    }
}
