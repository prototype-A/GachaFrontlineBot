package com.prototypeA.discordbot.GachaFrontline_Bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

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
				GuildAudioManager manager = GuildAudioManager.of(commandMessage.getGuildId().get());
				AudioPlayerManager playerManager = manager.getPlayerManager();
				
				playerManager.loadItemOrdered(manager, url, new AudioLoadResultHandler() {
					@Override
					public void trackLoaded(AudioTrack track) {
						manager.queue(track, gateway, commandMessage);
						deleteCommandMessage();
					}

					@Override
					public void playlistLoaded(AudioPlaylist playlist) {
						manager.queue(playlist, gateway, commandMessage);
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
		}
	}
}
