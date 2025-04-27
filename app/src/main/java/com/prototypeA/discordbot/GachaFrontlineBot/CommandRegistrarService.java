package com.prototypeA.discordbot.GachaFrontlineBot;

import com.prototypeA.discordbot.GachaFrontlineBot.handlers.AbstractSlashCommandHandler;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.interaction.GuildCommandRegistrar;
import discord4j.rest.RestClient;

import reactor.core.publisher.Mono;


/**
 * Service that registers/updates slash commands with Discord.
 */
@Service
public class CommandRegistrarService {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRegistrarService.class);
    
    private final RestClient CLIENT;
    private final List<ApplicationCommandRequest> GLOBAL_COMMANDS;
    private final List<ApplicationCommandRequest> SERVER_COMMANDS;

    /**
     * Constructs a new service to register/update slash commands.
     * 
     * @param restClient This application gateway's REST client.
     * @param commands The list of this application's slash commands.
     */
    @Lazy
    CommandRegistrarService(RestClient restClient, List<AbstractSlashCommandHandler> commands) {
        CLIENT = restClient;
        GLOBAL_COMMANDS = List.of();
        SERVER_COMMANDS = commands.stream()
            .map(AbstractSlashCommandHandler::getCommandRequest)
            .toList();
    }


    /**
     * Registers slash commands that can be used in both servers and 
     * direct messages with this application's bot user.
     */
    @PostConstruct
    private void registerGlobalCommands() {
        CLIENT.getApplicationService()
            .bulkOverwriteGlobalApplicationCommand(
                CLIENT.getApplicationId()
                    .block(),
                GLOBAL_COMMANDS)
            .doOnComplete(() -> LOG.info("Registered Global Slash Commands"))
            .doOnError(error -> LOG.error("Failed to register Global Slash Commands", error))
            .onErrorResume(_ -> Mono.empty())
            .subscribe();
    }

    /**
     * Registers slash commands that can be only used in servers.
     * 
     * @param guildId The ID of the server.
     */
    public void registerServerCommands(long serverId) {
        GuildCommandRegistrar.create(CLIENT, SERVER_COMMANDS)
            .registerCommands(Snowflake.of(serverId))
            .doOnComplete(() -> LOG.info("Registered Slash Commands for Server: {}", serverId))
            .doOnError(error -> LOG.error("Failed to register Slash Commands for Server: " + serverId, error))
            .onErrorResume(_ -> Mono.empty())
            .subscribe();
    }
}
