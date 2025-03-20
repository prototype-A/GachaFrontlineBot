package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import discord4j.core.event.domain.Event;

import reactor.core.publisher.Mono;


/**
 * Interface that serves as the base of implementing handlers 
 * of various Discord events dispatched by the gateway client.
 */
public interface IEventHandler<T extends Event> {

    /**
     * Returns the class of the event type that the 
     * implementing handler will handle.
     * 
     * @param <T> The type of the emitted Discord event that this 
     * handler is tasked to handle.
     * @return The class of the event to handle.
     */
    public Class<T> getEventClass();

    /**
     * Returns the name of this class.
     * 
     * @return The name of this event handler.
     */
    public default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * The method that is executed upon receiving an event of the 
     * type that the implementing handler is tasked to handle.
     * 
     * @param <T> The type of the emitted Discord event that this 
     * handler is tasked to handle.
     * @param event The event to handle.
     * @return An empty {@code Mono} upon completion of event handling.
     */
    public Mono<Void> handle(T event);

    /**
     * The method that is executed when an error occurs 
     * during the handling of an event.
     * 
     * @param error The error thrown.
     * @return An empty {@code Mono} after handling the error.
     */
    public Mono<Void> handleError(Throwable error);
}
