package com.prototypeA.discordbot.GachaFrontlineBot.permissions;

import com.prototypeA.discordbot.GachaFrontlineBot.ServerSettingsRepository;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;


/**
 * Factory for retrieving the permissions of servers based on 
 * the settings saved to this application at the time of retrieval.
 */
@Component
public final class Permissions {

    private final GatewayDiscordClient GATEWAY;
    private final ServerSettingsRepository SERVER_SETTINGS;

    /**
     * Constructs a new server permissions factory.
     * 
     * @param gateway This application's gateway client.
     * @param serverSettings The repository of this application's 
     * stored server settings.
     */
    @Lazy
    public Permissions(GatewayDiscordClient gateway, ServerSettingsRepository serverSettings) {
        GATEWAY = gateway;
        SERVER_SETTINGS = serverSettings;
    }

    /**
     * Returns the permissions for the specified server.
     * 
     * @param serverId The ID of the server to retrieve the 
     * permissions of.
     * @return The permissions of the server.
     */
    public ServerPermissions of(Snowflake serverId) {
        return new ServerPermissions(
            GATEWAY.getGuildById(serverId)
                .map(server -> server.getOwnerId())
                .block(),
            SERVER_SETTINGS.getServerSettings(serverId.asLong())
        );
    }
}
