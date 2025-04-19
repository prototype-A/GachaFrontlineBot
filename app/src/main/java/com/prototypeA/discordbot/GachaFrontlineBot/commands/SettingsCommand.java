package com.prototypeA.discordbot.GachaFrontlineBot.commands;

import com.prototypeA.discordbot.GachaFrontlineBot.Setting;
import com.prototypeA.discordbot.GachaFrontlineBot.handlers.AbstractSlashCommandHandler;
import com.prototypeA.discordbot.GachaFrontlineBot.util.MessageUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;


@Component
public class SettingsCommand extends AbstractSlashCommandHandler {

    private final int NUM_MAX_SELECTIONS = 25; // Limited by Discord
    private final String SELECT_MENU_ID = "dropdown";
    private final String LEFT_BUTTON_ID = "left-button";
    private final String RIGHT_BUTTON_ID = "right-button";
    private final Duration TIMEOUT_DURATION = Duration.ofSeconds(30);

    /**
     * Constructs a new handler to handle changing this application's settings.
     * 
     * @param serverSettings Repository to retrieve saved server settings from.
     */
    public SettingsCommand() {
        super("settings", "Adjust server preferences for this application.", List.of(
            new Setting(
                "Admin Roles",
                "The roles within this server that if users have, are able to use " +
                    "the administrative functionality (e.g. changing settings) of this " +
                    "application. The server owner is unaffected by this setting and " +
                    "is the only user allowed by default if this is left empty. " +
                    "Up to a maximum of 25 roles can be selected.",
                "",
                Setting.Type.Role
            ),
            new Setting(
                "Admin Users",
                "The specific users within this server that are able to use the " +
                    "administrative functionality of this application (e.g. changing " +
                    "settings). By default, this is limited to just the server owner, " +
                    "who is unaffected by this setting. Up to a maximum of 25 users " +
                    "can be selected.",
                "",
                Setting.Type.User
            ),
            new Setting(
                "Blacklisted Roles",
                "The roles within this server that if users have, are not allowed " +
                    "to use any functionality of this application. This takes precedence " +
                    "over all other settings. The server owner is unaffected by this setting.",
                "",
                Setting.Type.Role
            ),
            new Setting(
                "Blacklisted Users",
                "The specific users within this server that are not allowed to use " +
                    "any functionality of this application. This takes precedence over " +
                    "all other settings. The server owner is unaffected by this setting.",
                "",
                Setting.Type.User
            ),
            new Setting(
                "Whitelisted Roles",
                "The roles within this server that only the users which have " +
                    "it are allowed to use the (non-administrative) functionality " +
                    "of this application. This does not affect the server owner.",
                "",
                Setting.Type.Role
            )
        ), true, true, true);
    }


    @Override
    public ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
            .name(COMMAND_NAME)
            .description(COMMAND_DESC)
            .build();
    }

    /**
     * Returns the ID of the server that the specified event was emitted from.
     * 
     * @param event The emitted event to retrieve the server ID of.
     * @return The server ID as a Discord Snowflake that the event was received from.
     */
    private Snowflake getServerId(Event event) {
        if (event instanceof ChatInputInteractionEvent) {
            return ((ChatInputInteractionEvent) event).getInteraction()
                .getGuildId()
                .get();
        } else if (event instanceof MessageCreateEvent) {
            return ((MessageCreateEvent) event).getGuildId()
                .get();
        }
        return Snowflake.of(0);
    }

    /**
     * Sends a message reply that allows the server owner, specified users in the 
     * <b>Admin Users</b> setting, or users with one of the roles specified in the
     * <b>Admin Roles</b> setting to cycle through and edit all of this application's 
     * settings of its various functionality.
     * 
     * @param event The event emitted when this command is invoked 
     * (either {@code ChatInputInteractionEvent} for a slash command or 
     * {@code MessageCreateEvent} if invoked through a sent message).
     * @return An empty {@code Mono} upon completion of execution.
     */
    private Mono<Void> displaySettingsPanel(Event event) {
        long serverId = getServerId(event).asLong();
        List<Setting> serverSettingsList = serverSettings.getServerSettings(serverId)
            .values()
            .stream()
            .toList();
        int numSettings = serverSettingsList.size();
        AtomicInteger settingNum = new AtomicInteger(0);

        // Create interaction/message-specific IDs
        String identifier = "";
        if (event instanceof ChatInputInteractionEvent) {
            identifier += ((ChatInputInteractionEvent) event).getInteraction()
                .getId()
                .asLong();
        } else if (event instanceof MessageCreateEvent) {
            identifier += ((MessageCreateEvent) event).getMessage()
                .getId()
                .asLong();
        }
        String dropdownId = SELECT_MENU_ID + identifier;
        String leftButtonId = LEFT_BUTTON_ID + identifier;
        String rightButtonId = RIGHT_BUTTON_ID + identifier;

        // Buttons to cycle through settings to change
        Button leftButton = Button.primary(leftButtonId, ReactionEmoji.unicode("\u2B05"));
        Button rightButton = Button.primary(rightButtonId, ReactionEmoji.unicode("\u27A1"));

        // Component interaction event emitter
        Sinks.Many<ComponentInteractionEvent> componentInteractions = Sinks.many()
            .multicast()
            .onBackpressureBuffer();

        // Handle select menu interactions
        Disposable selectMenuHandler = event.getClient()
            .on(SelectMenuInteractionEvent.class, selection -> {
                // Interacted with by the same user that invoked the command
                if (isInvokingUser(
                        event,
                        selection.getInteraction()
                            .getUser()
                            .getId()
                    ) && dropdownId.equals(selection.getCustomId())) {
                    // Update server setting
                    serverSettings.saveServerSetting(
                        serverId,
                        serverSettingsList.get(settingNum.get()),
                        selection.getValues()
                            .toString()
                            .replace("[", "")
                            .replace("]", "")
                    );

                    // Emit event to refresh timeout duration
                    componentInteractions.tryEmitNext(selection);
    
                    // Acknowledge interaction and remove loading status
                    return selection.deferEdit()
                        .withEphemeral(true);
                }

                return Mono.empty();
            })
            .subscribe();

        // Handle cycling through settings to edit
        Disposable buttonHandler = event.getClient()
            .on(ButtonInteractionEvent.class, click -> {
                // Interacted with by the same user that invoked the command
                if (isInvokingUser(
                        event,
                        click.getInteraction()
                            .getUser()
                            .getId()
                    )) {
                    // Emit event to refresh timeout duration
                    componentInteractions.tryEmitNext(click);

                    String eventButtonId = click.getCustomId();
                    if (leftButtonId.equals(eventButtonId)) {
                        // Previous setting
                        if (settingNum.get() - 1 >= 0) {
                            settingNum.decrementAndGet();
                        } else {
                            settingNum.addAndGet(numSettings - 1);
                        }
                    } else if (rightButtonId.equals(eventButtonId)) {
                        // Next setting
                        settingNum.set((settingNum.get() + 1) % numSettings);
                    }
                    return click.edit(getSettingsPanelSpec(serverId, dropdownId, serverSettingsList.get(settingNum.get()))
                        .addComponent(ActionRow.of(leftButton, rightButton))
                        .build());
                }

                return Mono.empty();
            })
            .subscribe();

        // Time out after some period of no interaction from interaction components
        Mono<Void> timeoutFlux = Mono.when(componentInteractions.asFlux()
            .timeout(TIMEOUT_DURATION));

        // Add cycling buttons
        InteractionApplicationCommandCallbackSpec replySpec = getSettingsPanelSpec(serverId, dropdownId, serverSettingsList.get(settingNum.get()))
            .addComponent(ActionRow.of(leftButton, rightButton))
            .build();
            
        // Reply to command
        if (event instanceof ChatInputInteractionEvent) {
            ChatInputInteractionEvent command = (ChatInputInteractionEvent) event;
            return command.reply(replySpec)
                // Stop listening for component inputs after some timeout period
                .then(timeoutFlux.onErrorResume(TimeoutException.class, _ -> {
                    // Stop handling interactions after timeout
                    selectMenuHandler.dispose();
                    buttonHandler.dispose();
                    unlockCommandForServer(command.getInteraction()
                        .getGuildId()
                        .get());

                    // Delete reply
                    return ((ChatInputInteractionEvent) event).deleteReply();
                }));
        } else if (event instanceof MessageCreateEvent) {
            Message message = ((MessageCreateEvent) event).getMessage();
            return message.getChannel()
                .flatMap(channel -> channel.createMessage(MessageCreateSpec.builder()
                    .content(replySpec.content())
                    .components(replySpec.components())
                    .build()))
                .flatMap(reply -> Mono.when(timeoutFlux.onErrorResume(TimeoutException.class, _ -> {
                    // Stop handling interactions after timeout
                    selectMenuHandler.dispose();
                    buttonHandler.dispose();
                    unlockCommandForServer(message.getGuildId()
                        .get());

                    // Delete initial message and reply
                    return reply.delete()
                        .then(message.delete());
                })));
        }

        return Mono.empty();
    }

    /**
     * Returns the unbuilt slash command message reply builder object with 
     * the body of the message filled based on the passed setting parameter. 
     * This is to allow the addition of other components unrelated to the 
     * setting before building (and sending) the final message.
     * 
     * @param serverId The ID of the server this command is running in.
     * @param dropdownId The ID of the dropdown menu that it will emit events under.
     * @param setting The setting to build the message body around.
     * @return The unbuilt Builder object.
     */
    private InteractionApplicationCommandCallbackSpec.Builder getSettingsPanelSpec(
            long serverId,
            String dropdownId,
            Setting setting) {
        // Different dropdown menu depending on setting type
        // Setting.Type.Boolean
        SelectMenu dropdown = SelectMenu.of(
            dropdownId,
            SelectMenu.Option.of("True", "True")
                .withDefault("True".equals(setting.getValue())),
            SelectMenu.Option.of("False", "False")
                .withDefault("False".equals(setting.getValue()))
        );
        if (setting.getType() == Setting.Type.Role) {
            dropdown = SelectMenu.ofRole(dropdownId)
                .withMinValues(0)
                .withMaxValues(NUM_MAX_SELECTIONS)
                .withPlaceholder("No role(s) selected");
        } else if (setting.getType() == Setting.Type.String) {
            // Generate options
            String prefixes = "~`!@#$%^&*-_=+|;:',.<>?";
            SelectMenu.Option[] prefixOptions = new SelectMenu.Option[prefixes.length()];
            for (int c = 0; c < prefixes.length(); c++) {
                String prefix = String.valueOf(prefixes.charAt(c));
                String currPrefix = serverSettings.getServerSettings(serverId)
                    .get("Message Command Prefix")
                    .getValue();
                if (currPrefix.equals(prefix)) {
                    // Current prefix
                    prefixOptions[c] = SelectMenu.Option.ofDefault(currPrefix, currPrefix);
                } else {
                    prefixOptions[c] = SelectMenu.Option.of(prefix, prefix);
                }
            }
            dropdown = SelectMenu.of(dropdownId, prefixOptions);
        } else if (setting.getType() == Setting.Type.User) {
            dropdown = SelectMenu.ofUser(dropdownId)
                .withMinValues(0)
                .withMaxValues(NUM_MAX_SELECTIONS)
                .withPlaceholder("No user(s) selected");
        }

        return InteractionApplicationCommandCallbackSpec.builder()
            .ephemeral(true)
            .content(String.format(
                "%s\n\n%s",
                setting.getName(),
                setting.getDescription()
            ))
            .components(List.of(ActionRow.of(dropdown)));
    }

    @Override
    protected Mono<Void> run(ChatInputInteractionEvent event) {
        Snowflake serverId = getServerId(event);
        if (!lockCommandForServer(serverId)) {
            // Command currently in use
            return MessageUtils.sendReply(
                event,
                inUseMessage,
                true,
                Boolean.parseBoolean(serverSettings.getServerSettings(serverId.asLong())
                    .get("Ping Invoker")
                    .getValue())
            );
        }

        return Mono.just(event)
            .flatMap(this::displaySettingsPanel);
    }

    @Override
    public Mono<Void> run(MessageCreateEvent event) {
        Snowflake serverId = getServerId(event);
        if (!lockCommandForServer(serverId)) {
            // Command currently in use
            return MessageUtils.sendReply(
                event,
                inUseMessage,
                Boolean.parseBoolean(serverSettings.getServerSettings(serverId.asLong())
                    .get("Ping Invoker")
                    .getValue())
            );
        }

        return Mono.just(event)
            .flatMap(this::displaySettingsPanel);
    }
}
