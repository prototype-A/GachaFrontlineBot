package com.prototypeA.discordbot.GachaFrontline_Bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.LinkedList;
import java.util.Queue;


public final class AudioTrackScheduler extends AudioEventAdapter {

	private final Queue<AudioTrack> trackQueue;
	private final AudioPlayer player;


	public AudioTrackScheduler(final AudioPlayer player) {
		trackQueue = new ConcurrentLinkedQueue(new LinkedList<>());
		this.player = player;
	}


	public Queue<AudioTrack> getQueue() {
		return trackQueue;
	}

	public boolean queue(final AudioTrack track) {
		return play(track, true);
	}

	private boolean play(final AudioTrack track, final boolean queue) {
		final boolean playing = player.startTrack(track, queue);

		if (!playing) {
			trackQueue.add(track);
		}

		return playing;
	}

	public boolean skip() {
		return !trackQueue.isEmpty() && play(trackQueue.poll(), false);
	}


	@Override
	public void onPlayerPause(AudioPlayer player) {
		// Player was paused
		
	}

	@Override
	public void onPlayerResume(AudioPlayer player) {
		// Player was resumed
		
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		// A track started playing
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
			skip();
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
