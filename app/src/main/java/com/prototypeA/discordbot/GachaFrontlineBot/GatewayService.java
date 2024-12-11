package com.prototypeA.discordbot.GachaFrontlineBot;

import com.prototypeA.discordbot.GachaFrontlineBot.handlers.IEventHandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.ReadyEvent;


@Service
public class GatewayService {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayService.class);


    @Bean
    @Description("Logs in to the gateway of the configured DiscordClient, becoming an online user and registering event handlers.")
    @Scope("singleton")
    public <T extends Event> GatewayDiscordClient gatewayDiscordClient(DiscordClient discordClient, List<IEventHandler<T>> eventHandlers) {
        // Log into gateway
        GatewayDiscordClient gateway = discordClient.gateway()
            .withEventDispatcher(dispatcher -> dispatcher.on(ReadyEvent.class)
                .doOnNext(readyEvent -> {
                    LOG.info("Ready: {}", readyEvent.getShardInfo());
                    LOG.info("Logged in as: {}", readyEvent.getSelf().getUsername());
                }))
            .login()
            .block();

        // Register event handlers
        for (IEventHandler<T> handler: eventHandlers) {
            LOG.info("Registering {}", handler.getClass().getSimpleName());
            
            gateway.on(handler.getEventClass())
                .flatMap(handler::handle)
                .onErrorResume(handler::handleError)
                .subscribe();
        }

        return gateway;
    }
}
