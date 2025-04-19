package com.prototypeA.discordbot.GachaFrontlineBot.commands;

import com.prototypeA.discordbot.GachaFrontlineBot.handlers.AbstractSlashCommandHandler;
import com.prototypeA.discordbot.GachaFrontlineBot.util.MessageUtils;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

import reactor.core.publisher.Mono;


@Component
public class DieRollCommand extends AbstractSlashCommandHandler {

    private final long DEFAULT_NUM_SIDES = 6;
    private final long DEFAULT_NUM_TIMES = 1;
    private final String SIDES_PARAM_NAME = "sides";
    private final String AMOUNT_PARAM_NAME = "amount";

    public DieRollCommand() {
        super("roll", "Generates pseudo-random dice roll results.");
    }


    @Override
    public ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
            .name(COMMAND_NAME)
            .description(COMMAND_DESC)
            .options(List.of(
                ApplicationCommandOptionData.builder()
                    .name(SIDES_PARAM_NAME)
                    .description("How many sides should the die have?")
                    .type(4)
                    .minValue(3d)
                    .build(),
                ApplicationCommandOptionData.builder()
                    .name(AMOUNT_PARAM_NAME)
                    .description("How many times to roll the specified die")
                    .type(4)
                    .minValue(1d)
                    .build()
            ))
            .build();
    }

    /**
     * Returns the results of a specified die rolled a specified number 
     * of times using the same (pseudo-)random number generator (and seed).
     * 
     * @param numFaces The number of sides the die should have.
     * @param numTimes The number of times to roll the specified die.
     * @return A formatted message of the results.
     */
    private String getRolls(long numFaces, long numTimes) {
        Random rngesus = new Random();

        String rolls = "";
        for (long i = 0; i < numTimes; i++) {
            rolls += String.format("%s%s", (i > 0) ? ", " : "", rngesus.nextLong(numFaces) + 1);
        }

        return String.format("You rolled: %s", rolls);
    }

    @Override
    protected Mono<Void> run(ChatInputInteractionEvent event) {
        long numFaces = DEFAULT_NUM_SIDES;
        long numTimes = DEFAULT_NUM_TIMES;

        // Get passed parameters
        try {
            numFaces = event.getOption(SIDES_PARAM_NAME)
                .get()
                .getValue()
                .get()
                .asLong();
        } catch (Exception e) {}
        try {
            numTimes = event.getOption(AMOUNT_PARAM_NAME)
                .get()
                .getValue()
                .get()
                .asLong();
        } catch (Exception e) {}

        return MessageUtils.sendReply(
            event,
            getRolls(numFaces, numTimes),
            false,
            Boolean.valueOf(serverSettings.getServerSettings(numTimes)
                .get("Ping Invoker")
                .getValue())
        );
    }

    @Override
    public Mono<Void> run(MessageCreateEvent event) {
        long numFaces = DEFAULT_NUM_SIDES;
        long numTimes = DEFAULT_NUM_TIMES;

        // Get passed parameters
        String[] message = MessageUtils.getMessageContent(event)
            .split(" ");
        try {
            numFaces = Integer.parseInt(message[1]);
        } catch (Exception e) {}
        try {
            numTimes = Integer.parseInt(message[2]);
        } catch (Exception e) {}

        return MessageUtils.sendReply(
            event,
            getRolls(numFaces, numTimes),
            Boolean.valueOf(serverSettings.getServerSettings(numTimes)
                .get("Ping Invoker")
                .getValue())
        );
    }
}
