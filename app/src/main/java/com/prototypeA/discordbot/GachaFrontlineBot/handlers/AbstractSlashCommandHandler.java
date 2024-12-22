package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;

import reactor.core.publisher.Mono;


/**
 * The base of extending handlers tasked with running commands 
 * invoked through slash ("/") interactions
 */
public abstract class AbstractSlashCommandHandler extends AbstractCommandHandler<ChatInputInteractionEvent> {

    protected AbstractSlashCommandHandler(String commandName) {
        super(commandName);
    }


    /**
     * Returns the JSON representation of this command 
     * to be sent to Discord in order to register it as 
     * a guild/global slash command
     * 
     * @return The JSON representation of this command
     */
    public abstract ApplicationCommandRequest getCommandRequest();
    
    @Override
    public Class<ChatInputInteractionEvent> getEventClass() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public String getName() {
        return String.format("%s (/%s)", super.getName(), commandName);
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return Mono.just(event)
            // Ignore bot requests
            .filter(command -> !command.getInteraction()
                .getUser()
                .isBot())
            // Run invoked command
            .filter(command -> command.getCommandName()
                .equalsIgnoreCase(commandName))
            .flatMap(this::run);
    }

    /**
     * The method that contains the code to run when 
     * the command is invoked via a sent message
     * 
     * @param event The event containing the message sent
     * @return An empty Mono upon completion of execution
     */
    public abstract Mono<Void> run(MessageCreateEvent event);
}
