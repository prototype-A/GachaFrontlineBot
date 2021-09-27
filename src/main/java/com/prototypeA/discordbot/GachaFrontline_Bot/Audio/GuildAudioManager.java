package com.prototypeA.discordbot.GachaFrontline_Bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.voice.AudioProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Queue;


public final class GuildAudioManager {

	private static final AudioPlayerManager PLAYER_MANAGER;

	static {
		// Creates AudioPlayer instances and translates URLs to AudioTrack instances
		PLAYER_MANAGER = new DefaultAudioPlayerManager();

		// This is an optimization strategy that Discord4J can utilize to minimize allocations
		PLAYER_MANAGER.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

		// Allow playerManager to parse remote sources like YouTube links
		AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);

		// Allow playerManager to parse local audio file sources
		AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
	}


	private static final Map<Snowflake, GuildAudioManager> MANAGERS = new ConcurrentHashMap<>();


	public static GuildAudioManager of(final Snowflake id) {
		return MANAGERS.computeIfAbsent(id, ignored -> new GuildAudioManager(id));
	}

	private final Snowflake guildId;
	private final AudioPlayer player;
	private final AudioTrackScheduler scheduler;
	private final AudioProvider provider;

	private GuildAudioManager(Snowflake guildId) {
		this.guildId = guildId;
		player = PLAYER_MANAGER.createPlayer();
		scheduler = new AudioTrackScheduler(player);
		provider = new LavaPlayerAudioProvider(player);

		player.addListener(scheduler);
	}


	/**
	 * Gets the audio provider that will provide Discord with 
	 * the audio data from LavaPlayer
	 *
	 * @return The audio provider for the audio player
	 */
	public AudioProvider getAudioProvider() {
		return this.provider;
	}

	public Queue<AudioTrack> getPlaylist() {
		return scheduler.getQueue();
	}

	public int getPlayerVolume() {
		return player.getVolume();
	}

	public void setPlayerVolume(int volume) {
		player.setVolume(volume);
	}

	/**
	 * Queues up the audio from the specified url
	 *
	 * @param url The link of the audio source to play
	 * @return True if the bot successfully queued up the audio to play, otherwise False
	 */
	public void queue(String url) {
		try {
			//String filePath = AudioCore.getAudioFile(url, guildId).getCanonicalPath();

			//PLAYER_MANAGER.loadItem(url, new AudioLoadResultHandler() {
			//PLAYER_MANAGER.loadItem(filePath, new AudioLoadResultHandler() {
			PLAYER_MANAGER.loadItemOrdered(this, url, new AudioLoadResultHandler() {
			//PLAYER_MANAGER.loadItemOrdered(this, filePath, new AudioLoadResultHandler() {
				@Override
				public void trackLoaded(AudioTrack track) {
					scheduler.queue(track);
				}

				@Override
				public void playlistLoaded(AudioPlaylist playlist) {
					for (AudioTrack track: playlist.getTracks()) {
						scheduler.queue(track);
					}
				}

				@Override
				public void noMatches() {
					// Notify the user that we've got nothing
					
				}

				@Override
				public void loadFailed(FriendlyException throwable) {
					// Notify the user that everything exploded
					
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
