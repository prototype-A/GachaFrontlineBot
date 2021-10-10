package com.prototypeA.discordbot.GachaFrontline_Bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
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
	 * Gets the audio player manager used to create audio
	 * players and obtain audio from network/local sources
	 * 
	 * @return The global audio player manager
	 */
	public AudioPlayerManager getPlayerManager() {
		return PLAYER_MANAGER;
	}

	/**
	 * Gets the audio provider that will provide Discord with 
	 * the audio data from LavaPlayer
	 *
	 * @return The audio provider for the audio player
	 */
	public AudioProvider getAudioProvider() {
		return provider;
	}

	/**
	 * Removes the embedded message with that guild's audio
	 * player info and playback controls
	 */
	public void removePlayerEmbed() {
		scheduler.removePlayerEmbed();
	}


	/**
	 * Queues up the specified audio track
	 *
	 * @param track The audio track to queue up
	 * @param gateway The Discord gateway of the bot
	 * @param message The message that queued up the audio
	 */
	public void queue(AudioTrack track, GatewayDiscordClient gateway, 
						Message message) {
		scheduler.queue(track, gateway, message);
	}

	/**
	 * Queues up the specified audio playlist
	 *
	 * @param track The audio playlist to queue up
	 * @param gateway The Discord gateway of the bot
	 * @param message The message that queued up the audio
	 */
	public void queue(AudioPlaylist playlist, GatewayDiscordClient gateway, 
						Message message) {
		scheduler.queue(playlist, gateway, message);
	}

	public void pausePlayback() {
		if (player.getPlayingTrack() != null && !player.isPaused()) {
			player.setPaused(true);
		}
	}

	public void resumePlayback() {
		if (player.getPlayingTrack() != null && player.isPaused()) {
			player.setPaused(false);
		}
	}

	public boolean skipPlayback() {
		return scheduler.skip();
	}

	public void clearPlayback() {
		scheduler.clearPlayback();
	}
}
