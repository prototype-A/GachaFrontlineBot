package com.prototypeA.discordbot.GachaFrontline_Bot;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import discord4j.voice.AudioProvider;

import java.nio.Buffer;
import java.nio.ByteBuffer;


public final class LavaPlayerAudioProvider extends AudioProvider {

	private final AudioPlayer player;
	private final MutableAudioFrame frame;


	public LavaPlayerAudioProvider(final AudioPlayer player) {
		// Allocate a ByteBuffer for Discord4J's AudioProvider to hold audio data for Discord
		super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));

		// Set LavaPlayer's AudioFrame to use the same buffer as Discord4J's
		frame = new MutableAudioFrame();
		frame.setBuffer(getBuffer());
		this.player = player;
	}

	@Override
	public boolean provide() {
		// AudioPlayer writes audio data to the AudioFrame
		final boolean didProvide = player.provide(frame);

		// If audio was provided, flip from write-mode to read-mode
		if (didProvide) {
			((Buffer)getBuffer()).flip();
		}

		return didProvide;
	}
}
