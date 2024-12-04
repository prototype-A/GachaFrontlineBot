package com.prototypeA.discordbot.GachaFrontlineBot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;


@Service
public class GatewayService {
    @Bean
    @Description("Logs in to the gateway of the configured DiscordClient, becoming an online user.")
    public GatewayDiscordClient gatewayDiscordClient(DiscordClient discordClient) {
        return discordClient.gateway()
            .login()
            .block();
    }
}
