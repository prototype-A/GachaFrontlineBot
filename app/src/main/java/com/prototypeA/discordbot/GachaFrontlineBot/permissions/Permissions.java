package com.prototypeA.discordbot.GachaFrontlineBot.permissions;

import com.prototypeA.discordbot.GachaFrontlineBot.ServerSettingsRepository;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;


@Component
public final class Permissions {

    private final GatewayDiscordClient GATEWAY;
    private final ServerSettingsRepository SERVER_SETTINGS;

    @Lazy
    public Permissions(GatewayDiscordClient gateway, ServerSettingsRepository serverSettings) {
        GATEWAY = gateway;
        SERVER_SETTINGS = serverSettings;
    }

    public ServerPermissions of(Snowflake serverId) {
        return new ServerPermissions(
            GATEWAY.getGuildById(serverId)
                .map(server -> server.getOwnerId())
                .block(),
            SERVER_SETTINGS.getServerSettings(serverId.asLong())
        );
    }
}
