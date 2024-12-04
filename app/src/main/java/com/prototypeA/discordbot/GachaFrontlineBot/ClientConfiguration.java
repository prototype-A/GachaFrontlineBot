package com.prototypeA.discordbot.GachaFrontlineBot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import discord4j.core.DiscordClient;


@Configuration
@PropertySources({
    @PropertySource("file:bot.properties"),
    @PropertySource(value = "file:${spring.profiles.active}-bot.properties", ignoreResourceNotFound = true)
})
public class ClientConfiguration {

    @Value("${token}")
    private String token;

    @Bean
    @Description("Configures and creates a (non-gateway) DiscordClient with the token provided in \"./bot.properties\"")
    public DiscordClient discordClient() {
        return DiscordClient.create(token);
    }
}