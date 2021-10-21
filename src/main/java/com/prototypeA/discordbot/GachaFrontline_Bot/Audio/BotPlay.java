package com.prototypeA.discordbot.GachaFrontline_Bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.Member;

import java.util.HashMap;
import java.util.Map;


public class BotPlay extends Command {

	public BotPlay() {
		this("play", "");
	}

	public BotPlay(String command, String subCommand) {
		super(command, subCommand, new CommandType[]{ CommandType.GUILD },
			"Queues up audio from a specified source to play",
			new CommandParameter[]{
				new CommandParameter("url",
					"The audio source to queue up from (e.g. A YouTube link)",
					3, true)
			}
		);

		// Add alternative ways to invoke this command
		addAlias("queue", "");
	}

	/**
	 * Queues up audio to be played in a guild
	 *
	 * @param url The source to queue up audio from
	 * @param channel The channel of the message/interaction that queued up the audio
	 * @param queuer The guild member that queued up the audio
	 */
	private void queueAudio(String url, TextChannel channel, Member queuer) {
		GuildAudioManager manager = GuildAudioManager.of(channel.getGuildId());
		AudioPlayerManager playerManager = manager.getPlayerManager();
				
		playerManager.loadItemOrdered(manager, url, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				manager.queue(track, gateway, channel, queuer);
				deleteCommandMessage();
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				manager.queue(playlist, gateway, channel, queuer);
				deleteCommandMessage();
			}

			@Override
			public void noMatches() {
				// Notify the user that we've got nothing
				sendTempMessage("Invalid/unsupported URL specified");
			}

			@Override
			public void loadFailed(FriendlyException e) {
				// Notify the user that everything exploded
				sendTempMessage("Failed to load audio: " + e.getMessage());
			}
		});
	}

	// Join user voice channel and queue up audio from a url
	public void run() {
		// Check for permissions
		if (canIssueBotCommands()) {
			// Get params
			String url = getAsOneArg();
			if (params.size() > 0) {
				url = params.get("url");
			}

			// Queue audio only if successful in joining the user's voice channel
			if (interaction != null && 
				joinUserVoiceChannel(interaction.getMember().get())) {
				Snowflake guildId = interaction.getGuildId().get();
				TextChannel channel = (TextChannel)interaction.getChannel().block();
				Member queuer = interaction.getMember().get();

				queueAudio(url, channel, queuer);
			} else if (joinUserVoiceChannel()) {
				Snowflake guildId = commandMessage.getGuildId().get();
				TextChannel channel = (TextChannel)commandMessage.getChannel().block();
				Member queuer = commandMessage.getAuthorAsMember().block();

				queueAudio(url, channel, queuer);
			}
		}
	}
}
