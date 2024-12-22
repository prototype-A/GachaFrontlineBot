package com.prototypeA.discordbot.GachaFrontlineBot;

import com.prototypeA.discordbot.GachaFrontlineBot.handlers.IEventHandler;

import java.util.List;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.rest.RestClient;


@Service
public class ClientService {

    private static final Logger LOG = LoggerFactory.getLogger(ClientService.class);

    @Autowired
    private ApplicationContext context;


    @Bean
    @Description("Logs into the gateway of the configured DiscordClient, becoming an online user and registering event handlers.")
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
            gateway.on(handler.getEventClass())
                .flatMap(handler::handle)
                .onErrorResume(handler::handleError)
                .subscribe();
            LOG.info("Registered Event Handler: {}", handler.getName());
        }
        
        return gateway;
    }

    @PreDestroy
    public void logout() {
        LOG.info("Program terminated, logging out of gateway");
        try {
            context.getBean(GatewayDiscordClient.class)
                .logout()
                .block();
        } catch (Exception e) {
            LOG.error("Failed to log out of gateway", e);
        }
    }

    @Bean
    @Description("Retrieves the REST client from the gateway used to execute Discord REST API requests")
    @Scope("singleton")
    public RestClient restClient(GatewayDiscordClient gateway) {
        return gateway.getRestClient();
    }
}
