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


/**
 * Command to generate pseudo-random coin flips.
 */
@Component
public class CoinFlipCommand extends AbstractSlashCommandHandler {

    private final long DEFAULT_NUM_TIMES = 1;
    private final String AMOUNT_PARAM_NAME = "amount";

    /**
     * Constructs a new command handler to generate pseudo-random coin flips.
     */
    public CoinFlipCommand() {
        super("flip", "Generates pseudo-random coin flip results.");
    }


    @Override
    public ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
            .name(COMMAND_NAME)
            .description(COMMAND_DESC)
            .options(List.of(
                ApplicationCommandOptionData.builder()
                    .name(AMOUNT_PARAM_NAME)
                    .description("How many times to flip a coin")
                    .type(4)
                    .minValue(1d)
                    .build()
            ))
            .build();
    }

    /**
     * Returns the results of a coin a specified number of times 
     * using the same (pseudo-)random number generator (and seed).
     * 
     * @param numTimes The number of times to flip a coin.
     * @return A formatted message of the results.
     */
    private String getCoinFlips(long numTimes) {
        Random rngesus = new Random();

        String flips = "";
        for (long i = 0; i < numTimes; i++) {
            flips += String.format(
                "%s%s",
                (i > 0)
                    ? ", "
                    : "",
                rngesus.nextInt(2) == 1
                    ? "Heads"
                    : "Tails");
        }

        return String.format("The coin landed on: %s", flips);
    }

    @Override
    protected Mono<Void> run(ChatInputInteractionEvent event) {
        long numTimes = DEFAULT_NUM_TIMES;

        // Get passed parameters
        try {
            numTimes = event.getOption(AMOUNT_PARAM_NAME)
                .get()
                .getValue()
                .get()
                .asLong();
        } catch (Exception e) {}

        return MessageUtils.sendReply(
            event,
            getCoinFlips(numTimes),
            false,
            Boolean.valueOf(serverSettings.getServerSettings(numTimes)
                .get("Ping Invoker")
                .getValue())
        );
    }

    @Override
    public Mono<Void> run(MessageCreateEvent event) {
        long numTimes = DEFAULT_NUM_TIMES;

        // Get passed parameters
        try {
            String[] message = MessageUtils.getMessageContent(event)
                .split(" ");
            numTimes = Integer.parseInt(message[1]);
        } catch (Exception e) {}

        return MessageUtils.sendReply(
            event,
            getCoinFlips(numTimes),
            Boolean.valueOf(serverSettings.getServerSettings(numTimes)
                .get("Ping Invoker")
                .getValue())
        );
    }
}
