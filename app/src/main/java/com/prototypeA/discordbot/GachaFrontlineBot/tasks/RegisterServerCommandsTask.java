package com.prototypeA.discordbot.GachaFrontlineBot.tasks;

import com.prototypeA.discordbot.GachaFrontlineBot.CommandRegistrarService;
import com.prototypeA.discordbot.GachaFrontlineBot.handlers.AbstractTaskHandler;

import java.util.List;

import org.springframework.stereotype.Component;

import discord4j.core.event.domain.guild.GuildCreateEvent;

import reactor.core.publisher.Mono;


/**
 * Task that automatically registers server-specific 
 * slash commands upon receiving a server through 
 * log-in, reconnection, or joining a new server.
 */
@Component
public final class RegisterServerCommandsTask extends AbstractTaskHandler<GuildCreateEvent> {

    private final CommandRegistrarService REGISTRAR;

    /**
     * Constructs a new task handler that registers server 
     * slash commands for new servers.
     * 
     * @param registrar The slash command registration service.
     */
    public RegisterServerCommandsTask(CommandRegistrarService registrar) {
        super(
            "Register Server Commands",
            "Registers server commands upon joining a new server.",
            List.of(),
            true
        );

        REGISTRAR = registrar;
    }


    @Override
    public Class<GuildCreateEvent> getEventClass() {
        return GuildCreateEvent.class;
    }

    @Override
    public Mono<Void> handle(GuildCreateEvent event) {
        LOG.info("Joined Server: {}", event.getGuild()
            .getId());
        REGISTRAR.registerServerCommands(event.getGuild()
            .getId()
            .asLong());

        return Mono.empty();
    }
}