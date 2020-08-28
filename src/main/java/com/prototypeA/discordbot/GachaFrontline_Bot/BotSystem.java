package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.VoiceChannelJoinSpec;
import discord4j.voice.VoiceConnection;

import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;


public class BotSystem extends Command {


	public BotSystem(String command) {
		this.command = command;
	}

	public void init(Message message, GatewayDiscordClient gateway) {
		super.init(message, gateway);
	}

	public void run() {

		// Check for permissions
		if (canIssueBotCommands()) {

			// Get arguments passed
			String[] args = getArgs();

			if (command.equals("goto")) {
				/**
				 * Attempts to join the specified Voice Channel, otherwise
				 * move to the sender's channel if none was given
				 */
				String voiceChannelName = getAsOneArg();
				joinVoiceChannel(voiceChannelName);
			}

			else if (command.equals("exit")) {
				try {
					// Attempt to leave the Voice Channel it currently is in (if in one)
					gateway.getSelf()
							.block()
							.asMember(cmdMessage.getGuild()
													.block()
													.getId())
							.block()
							.getVoiceState()
							.block()
							.getChannel()
							.block()
							.sendDisconnectVoiceState()
							.block();
				} catch (Exception e) {
					sendTempMessage("Currently not in a voice channel!");
				}
			}
		}
	}

	/**
	 * Attempts to connect to the voice channel with the
	 * specified name. If no channel name is specified, 
	 * attenpt to join the voice channel the user that
	 * issued the command is in (if any).
	 *
	 * @param channelName The name of the channel to join
	 */
	public void joinVoiceChannel(String channelName) {

		if (channelName == null || channelName.equals("")) {
			// No parameters: Attempt to join the voice channel of the message sender
			try {
				VoiceChannel channel = cmdMessage.getGuild()
										.block()
										.getMemberById(cmdMessage.getAuthor()
														.get()
														.getId())
										.block()
										.getVoiceState()
										.block()
										.getChannel()
										.block();
				joinVoiceChannel(channel, true);
			} catch (Exception e) {
				sendTempMessage("You are currently not in a voice channel!");
			}
		} else {
			// Attempt to join the specified channel
			VoiceChannel channel = null;
			try {
				// Find voice channel by name
				channel = (VoiceChannel)cmdMessage.getGuild()
													.block()
													.getChannels()
													.filter(chn -> chn.getType() == Channel.Type.GUILD_VOICE &&
																	chn.getName()
																	.toLowerCase()
																	.contains(channelName.toLowerCase()))
													.buffer()
													.blockLast()
													.get(0);
				// Join voice channel
				joinVoiceChannel(channel, true);
			} catch (NullPointerException e1) {
				sendTempMessage("Voice channel \"" + channelName + "\" not found.");
			} catch(Exception e2) {
				// Bot may not have sufficient permissions to join voice channel
				//e2.printStackTrace();
				sendTempMessage("An error occurred while attempting to connect to the voice channel \"" + channel.getName() + "\".\nIt may be due to lack of permissions.");
			}
		}
	}

	/**
	 * Attempts to connect to the specified voice channel
	 *
	 * @param channel The voice channel to join
	 */
	public void joinVoiceChannel(VoiceChannel channel, boolean showChannelStats) {
		if (channel != null) {
			Mono<VoiceConnection> voiceConnection = channel.join(spec -> {
																// Deafen self when joining
																spec.setSelfDeaf(true);
															});
			// Show voice channel stats after connecting to voice channel
			if (showChannelStats && voiceConnection.block().isConnected().block()) {
				showAudioStats(channel);
			}
			// Disconnect from voice channel after being alone in ot for 15s
			voiceConnection.flatMap(connection -> {
										// Check if bot is alone in voice channel
										final Publisher<Boolean> voiceStateCounter = channel.getVoiceStates()
																							.count()
																							.map(count -> 1L == count);
										// Check if bot is still alone after 15 seconds of joining voice channel
										final Mono<Void> onDelay = Mono.delay(Duration.ofSeconds(15L))
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
										return Mono.first(onDelay, onEvent).then(connection.disconnect());
									}).block();
		}
	}

	/**
	 * Displays audio statistics of the specified voice channel
	 *
	 * @param channel The voice channel to display the statistics of
	 */
	private void showAudioStats(VoiceChannel channel) {
		String msg = "Voice Channel:";
		msg += "\nBitrate: " + Integer.toString(channel.getBitrate() / 1000) + "Kbps";
		//msg += "\nBot Volume: " + Integer.toString((int)(audioPlayer.getVolume()*100)) + "%";
		sendTempMessage(msg);
	}


	/**
	 * Return the command-specific help String to BotHelp
	 */
	public String getHelp() {
		String helpMessage = "List of parameters for the '" + command + "' command:\n\n";

		if (command.equals("goto")) {
			helpMessage += "'goto'\nThe bot will attempt to join the Voice Channel that you are currently in (Only works if you are in a Voice Channel)\n\n";
			helpMessage += "'goto *channel_name*'\nThe bot will attempt to join the specified Voice Channel";
		}
		else if (command.equals("exit")) {
			helpMessage += "'exit'\nThe bot will disconnect from the Voice Channel it is currently in (Only works if it is currently in a Voice Channel)";
		}

		return helpMessage;
	}

}
