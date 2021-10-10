package com.prototypeA.discordbot.GachaFrontline_Bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.LinkedList;
import java.util.Queue;


public final class AudioTrackScheduler extends AudioEventAdapter {

	private final Queue<AudioTrack> trackQueue;
	private final AudioPlayer player;
	private PlayerEmbed embed;


	public AudioTrackScheduler(final AudioPlayer player) {
		trackQueue = new ConcurrentLinkedQueue(new LinkedList<>());
		this.player = player;
		this.embed = null;
	}


	public Queue<AudioTrack> getQueue() {
		return trackQueue;
	}

	public boolean queue(final AudioTrack track,
							final GatewayDiscordClient gateway, 
							final Message message) {
		track.setUserData(message.getAuthorAsMember().block());
		boolean playing = play(track, true);
		updatePlayerEmbed(gateway, message);

		return playing;
	}

	public void queue(final AudioPlaylist playlist, 
						final GatewayDiscordClient gateway, 
						final Message message) {
		for (AudioTrack track: playlist.getTracks()) {
			track.setUserData(message.getAuthorAsMember().block());
			play(track, true);
		}

		updatePlayerEmbed(gateway, message);
	}

	private boolean play(final AudioTrack track, final boolean queue) {
		final boolean playing = player.startTrack(track, queue);

		if (!playing) {
			trackQueue.add(track);
		}

		return playing;
	}

	public boolean skip() {
		// Last song in queue
		if (trackQueue.isEmpty() && player.getPlayingTrack() != null) {
			player.stopTrack();

			return true;
		}

		return !trackQueue.isEmpty() && play(trackQueue.poll(), false);
	}

	public void clearPlayback() {
		player.stopTrack();
		trackQueue.clear();
	}


	private void updatePlayerEmbed() {
		if (embed != null) {
			embed.update(player.getPlayingTrack(), !player.isPaused(),
				trackQueue);
		}
	}

	private void updatePlayerEmbed(final GatewayDiscordClient gateway, 
									final Message message) {
		if (embed == null && message != null) {
			// Create embed message if it doesn't exist
			embed = new PlayerEmbed(message, gateway, player.getPlayingTrack(),
									!player.isPaused(), trackQueue);
		} else {
			// Update the embed message
			updatePlayerEmbed();
		}
	}

	public void removePlayerEmbed() {
		if (this.embed != null) {
			PlayerEmbed embed = this.embed;
			this.embed = null;

			embed.delete();
		}
	}


	@Override
	public void onPlayerPause(AudioPlayer player) {
		// Player was paused
		updatePlayerEmbed();
	}

	@Override
	public void onPlayerResume(AudioPlayer player) {
		// Player was resumed
		updatePlayerEmbed();
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		// A track started playing
		updatePlayerEmbed();
	}

	@Override
	public void onTrackEnd(final AudioPlayer player, final AudioTrack track,
							final AudioTrackEndReason endReason) {
		/*
		endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
		endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
		endReason == STOPPED: The player was stopped.
		endReason == REPLACED: Another track started playing while this had not finished
		endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
								clone of this back to your queue
		*/

		// Advance the player if the track completed naturally (FINISHED)
		// or if the track cannot play (LOAD_FAILED)
		if (endReason.mayStartNext) {
			if (!trackQueue.isEmpty()) {
				skip();
			}
			updatePlayerEmbed();
		}
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
		
	}
}
