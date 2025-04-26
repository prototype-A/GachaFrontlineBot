package com.prototypeA.discordbot.GachaFrontlineBot.handlers;

import com.prototypeA.discordbot.GachaFrontlineBot.AbstractHasDefaultSettings;
import com.prototypeA.discordbot.GachaFrontlineBot.ServerSettingsRepository;
import com.prototypeA.discordbot.GachaFrontlineBot.Setting;
import com.prototypeA.discordbot.GachaFrontlineBot.permissions.Permissions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import discord4j.core.event.domain.Event;

import reactor.core.publisher.Mono;


/**
 * The base of all extending handlers tasked with automatically 
 * executing some functionality upon receiving an emitted 
 * instance of the event it is tasked to handle.
 */
public abstract class AbstractTaskHandler<T extends Event> extends AbstractHasDefaultSettings implements IEventHandler<T> {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected final String NAME;
    protected final String DESC;

    @Autowired
    protected Permissions permissions;
    @Autowired @Lazy
    private ServerSettingsRepository serverSettings;

    /**
     * Constructs a new handler that will automatically run upon receiving 
     * an emitted instance of the event that it is tasked to handle.
     * 
     * @param name The name of this task.
     * @param desc The description of this task.
     */
    protected AbstractTaskHandler(String name, String desc) {
        this(name, desc, List.of());
    }

    /**
     * Constructs a new handler that will automatically run upon receiving 
     * an emitted instance of the event that it is tasked to handle.
     * 
     * @param name The name of this task.
     * @param desc The description of this task.
     * @param defaultSettings The list of this task's available 
     * settings initialized with its default values.
     */
    protected AbstractTaskHandler(
            String name,
            String desc,
            List<Setting> defaultSettings) {
        this(name, desc, defaultSettings, false);
    }

    /**
     * Constructs a new handler that will automatically run upon receiving 
     * an emitted instance of the event that it is tasked to handle.
     * 
     * @param name The name of this task.
     * @param desc The description of this task.
     * @param defaultSettings The list of this task's available 
     * settings initialized with its default values.
     * @param cannotDisable Whether or not this task can be disabled.
     */
    protected AbstractTaskHandler(
            String name,
            String desc,
            List<Setting> defaultSettings,
            boolean cannotDisable) {
        super("Task: " + name, desc, defaultSettings, cannotDisable);

        NAME = name;
        DESC = desc;
    }


    @Override
    public Mono<Void> handleError(Throwable error) {
        LOG.error("Failed to handle task " + getName(), error);
        return Mono.empty();
    }
}
