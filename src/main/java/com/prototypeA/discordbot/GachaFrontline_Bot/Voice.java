package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.VoiceState;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.Duration;


public abstract class Voice extends Messaging {


	/**
	 * Attempts to connect to the voice channel that the
	 * sender of the command is in.
	 * 
	 * @return Whether the bot joined the voice channel or not 
	 * 			(user was or was not in a voice channel)
	 */
	public boolean joinUserVoiceChannel() {
		return joinUserVoiceChannel(commandMessage.getAuthor().get());
	}

	/**
	 * Attempts to connect to the voice channel that the
	 * specified user is in.
	 *
	 * @param user The user to join the voice channel of
	 * @return Whether the bot joined the voice channel or not 
	 * 			(user was or was not in a voice channel)
	 */
	public boolean joinUserVoiceChannel(User user) {
		if (user != null) {
			// Join the specified user's voice channel
			try {
				VoiceChannel channel = commandMessage.getGuild()
										.block()
										.getMemberById(user.getId())
										.block()
										.getVoiceState()
										.block()
										.getChannel()
										.block();

				return joinVoiceChannel(channel);
			} catch (Exception e) {
				e.printStackTrace();
				sendTempMessage("You are currently not in a voice channel");
			}
		}

		return false;
	}

	/**
	 * Attempts to connect to the specified voice channel.
	 * If joined, disconnect from the voice channel when 
	 * every user has left or if it is alone for a set 
	 * period of time.
	 *
	 * @param channel The voice channel to join
	 * @return Whether the bot joined the voice channel or not
	 */
	public boolean joinVoiceChannel(VoiceChannel channel) {
		boolean joined = false;

		if (channel != null) {
			GuildAudioManager manager = GuildAudioManager.of(channel.getGuild().block().getId());
			AudioProvider audioProvider = manager.getAudioProvider();
			Mono<VoiceConnection> voiceConnection = channel.join(spec -> {
														// Deafen self when joining
														spec.setSelfDeaf(true);
														// Set audio provider
														spec.setProvider(audioProvider);
													});
			if (voiceConnection.block().isConnected().block()) {
				joined = true;
			}

			// Disconnect from voice channel after being alone in it for 10s
			voiceConnection.flatMap(connection -> {
				// Check if bot is alone in voice channel
				final Publisher<Boolean> voiceStateCounter = channel.getVoiceStates()
																.count()
																.map(count -> 1L == count);

				// Check if bot is still alone after 10 seconds of joining voice channel
				final Mono<Void> onDelay = Mono.delay(Duration.ofSeconds(10L))
											.filterWhen(ignored -> voiceStateCounter)
											.switchIfEmpty(Mono.never())
											.then();

				// Check if bot is alone after someone leaves the channel
				final Mono<Void> onEvent = channel.getClient()
											.getEventDispatcher()
											.on(VoiceStateUpdateEvent.class)
											.filter(event -> event.getOld()
												.flatMap(VoiceState::getChannelId)
												.map(channel.getId()::equals)
												.orElse(false))
											.filterWhen(ignored -> voiceStateCounter)
											.next()
											.then();

				// Disconnect bot if either condition is met
				return Mono.first(new Mono[]{ onDelay, onEvent })
						.then(connection.disconnect()
							.doOnSuccess(s -> {
								manager.removePlayerEmbed();
								manager.clearPlayback();
							}));
			}).subscribe();
		}

		return joined;
	}
}
