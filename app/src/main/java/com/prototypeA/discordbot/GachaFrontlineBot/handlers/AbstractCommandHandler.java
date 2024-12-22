package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.core.event.domain.Event;

import reactor.core.publisher.Mono;


/**
 * The base of all extending handlers tasked with 
 * running commands invoked by users
 */
public abstract class AbstractCommandHandler<T extends Event> implements IEventHandler<T> {
    
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected final String commandName;

    protected AbstractCommandHandler(String commandName) {
        this.commandName = commandName;
    }


    /**
     * Returns the string that is used to invoke this command
     * 
     * @return The name of this command
     */
    public String getCommandName() {
        return this.commandName;
    }

    /**
     * The method that contains the code to run when 
     * the command is invoked
     * 
     * @param event The event containing the command and its relevent data
     * @return An empty Mono upon completion of execution
     */
    public abstract Mono<Void> run(T event);
}
