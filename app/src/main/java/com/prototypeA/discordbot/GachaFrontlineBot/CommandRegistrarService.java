package com.prototypeA.discordbot.GachaFrontlineBot;

import com.prototypeA.discordbot.GachaFrontlineBot.handlers.AbstractSlashCommandHandler;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.interaction.GuildCommandRegistrar;
import discord4j.rest.RestClient;

import reactor.core.publisher.Mono;


@Service
@PropertySources({
    @PropertySource("file:bot.properties"),
    @PropertySource(value = "file:${spring.profiles.active}-bot.properties", ignoreResourceNotFound = true)
})
public class CommandRegistrarService {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRegistrarService.class);
    
    private final RestClient client;
    private final List<ApplicationCommandRequest> commands;

    @Value("${guild}")
    private long guildId;

    public CommandRegistrarService(RestClient restClient, List<AbstractSlashCommandHandler> commands) {
        this.client = restClient;
        this.commands = commands.stream()
            .map(AbstractSlashCommandHandler::getCommandRequest)
            .toList();
    }


    @PostConstruct
    private void registerCommands() {
        registerGlobalCommands();
        registerGuildCommands(guildId);
    }

    public void registerGlobalCommands() {
        long appId = client.getApplicationId()
            .block();
        client.getApplicationService()
            .bulkOverwriteGlobalApplicationCommand(appId, commands)
            .doOnNext(_ -> LOG.info("Registered Global Slash Commands"))
            .doOnError(e -> LOG.error("Failed to register Global Slash Commands", e))
            .onErrorResume(_ -> Mono.empty())
            .subscribe();
    }

    public void registerGuildCommands(long guildId) {
        GuildCommandRegistrar.create(client, commands)
            .registerCommands(Snowflake.of(guildId))
            .doOnNext(_ -> LOG.info("Registered Slash Commands for Guild {}", guildId))
            .doOnError(e -> LOG.error("Failed to register Slash Commands for Guild " + guildId, e))
            .onErrorResume(_ -> Mono.empty())
            .subscribe();
    }
}
