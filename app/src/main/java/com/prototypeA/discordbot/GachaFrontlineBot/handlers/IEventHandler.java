package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.core.event.domain.Event;

import reactor.core.publisher.Mono;


public interface IEventHandler<T extends Event> {

    static final Logger LOG = LoggerFactory.getLogger(IEventHandler.class);

    /**
     * Returns the class of the event type that the 
     * implementing handler will handle
     * 
     * @return The class of the event to handle
     */
    public Class<T> getEventClass();

    /**
     * The method that executes upon receiving an event 
     * that the implementing handler is tasked to handle
     * 
     * @param event The event to handle
     * @return An empty mono upon completion of event handling
     */
    public Mono<Void> handle(T event);
    
    /**
     * The method that is executed when an error occurs 
     * during the handling of an event
     * 
     * @param error The error thrown
     * @return An empty mono
     */
    public default Mono<Void> handleError(Throwable error) {
        LOG.error("Failed to process " + getEventClass().getSimpleName(), error);
        return Mono.empty();
    }
}
