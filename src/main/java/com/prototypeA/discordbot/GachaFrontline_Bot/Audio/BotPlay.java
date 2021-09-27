package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;

import java.util.HashMap;
import java.util.Map;


public class BotPlay extends Command {

	public BotPlay() {
		super("play", "", new CommandType[]{ CommandType.GUILD },
			"Queues up audio from a specified source to play",
			new CommandParameter[]{
				new CommandParameter("URL",
					"The audio source to queue up from (e.g. YouTube link)",
					3, true)
			}
		);

		// Add alternative ways to invoke this command
		addAlias("queue", "");
	}


	// Queue up audio from a url
	public void run() {
		// Check for permissions
		if (canIssueBotCommands()) {
			String url = getAsOneArg();

			// Queue audio only if successful in joining the user's voice channel
			if (joinUserVoiceChannel()) {
				GuildAudioManager.of(commandMessage.getGuildId().get()).queue(url);
			}
		}
	}
}
