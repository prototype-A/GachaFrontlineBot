package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.Arrays;
import java.util.Iterator;


public class BotSystem extends Command {

	private AudioPlayer audioPlayer;
	private IUser author;


	public BotSystem(String command) {
		this.command = command;
	}

	public void init(IMessage message, IDiscordClient bot) {
		super.init(message, bot);
		author = message.getAuthor();
		audioPlayer = AudioPlayer.getAudioPlayerForGuild(guild);
	}

	public void run() {

		// Check for permissions
		if (canIssueBotCommands()) {

			// Get arguments passed
			String[] args = getArgs();

			if (command.equals("goto")) {
				/**
				 * Tells the bot to move to a given Voice Channel
				 * or move to the sender's channel if none are given
				 */
				String voiceChannelName = getAsOneArg();
		
				// No parameters: Attempt to join the voice channel of the sender of the message
				if (voiceChannelName == null) {
					try {
						// Discord4J 2.8.4 Implement
						IVoiceChannel voiceChannel = cmdMessage.getAuthor().getVoiceStateForGuild(cmdMessage.getGuild()).getChannel();
						voiceChannel.join();
						showAudioStats(voiceChannel);
					} catch (MissingPermissionsException e) {
						missingPermissionsWarning(e);
						sendTempMessage("Insufficient permissions to join voice channel.");
					} catch (Exception e) {
						sendTempMessage("You are currently not in a voice channel.");
					}
				}
				// Tell bot to join a specified channel
				else {
					try {
						IGuild currGuild = cmdMessage.getGuild();
						Iterator<IVoiceChannel> channelIter = currGuild.getVoiceChannels().iterator();
				
						boolean channelNotFound = true;
						// Check if requested channel is in the set of channels available
						while (channelNotFound && channelIter.hasNext()) {
							IVoiceChannel voiceChannel = channelIter.next();
							if (voiceChannel.getName().equals(voiceChannelName)){
								channelNotFound = false;
								voiceChannel.join();
								showAudioStats(voiceChannel);
							}
						}
						if (channelNotFound) {
							sendTempMessage("Voice channel \"" + voiceChannelName + "\" not found");
						}
					} catch(Exception e) {
						e.printStackTrace();
						Main.displayError("An error occurred while attempting to join a voice channel.");
					}
				}
			}

			else if (command.equals("exit")) {
				/**
				 * Has the bot exit the Voice Channel it currently is in
				 */
				IVoiceChannel currVoiceChannel = botClient.getOurUser().getVoiceStateForGuild(cmdMessage.getGuild()).getChannel();
				if (currVoiceChannel == null) {
					sendMessage("Currently not in a voice channel!");
				} else {
					currVoiceChannel.leave();
				}
			}
			/*
			else if (command.equals("updateavatar")) {

				/*
				 * Try to change bot avatar
				 *

				// No url passed
				if (args.length == 0) {
					sendMessage("No url specified.");
				}
				else {
					try {
						String imageFormat = args[0].substring(args[0].lastIndexOf('.') + 1, args[0].lastIndexOf('.') + 4);
						String imageUrl = args[0].substring(args[0].lastIndexOf(' ') + 1);
						botClient.changeAvatar(Image.forUrl(imageFormat, imageUrl));
						Main.displayMessage("Avatar successfully changed to: " + imageUrl);
					}
					catch (Exception e) {
						Main.displayError(e + " occurred while changing the avatar.");
					}
				}
			}

			else if (command.equals("updatenickname")) {
				/*
				 * Try to change bot nickname
				 *
				String newNickname = getAsOneArg();

				// No string passed
				if (args.length == 0) {
					sendMessage("No string passed.");
				}
				else {
					try {
						//String newNickname = Arrays.toString(args).replace("[", "").replace(",", "").replace("]", "");
						cmdMessage.getGuild().setUserNickname(botClient.getOurUser(), newNickname);
						Main.displayMessage("Nickname successfully changed to: " + newNickname);
					}
					catch (Exception e) {
						Main.displayError(e + " occurred while changing the nickname.");
					}
				}
			}
			*/
		}

	}

	/**
	 * Display audio statistics after joining a voice channel
	 */
	private void showAudioStats(IVoiceChannel channel) {
		String msg = "Voice channel stats:";
		msg += "\nBitrate: " + Integer.toString(channel.getBitrate() / 1000) + "Kbps";
		msg += "\nCurrent bot volume: " + Integer.toString((int)(audioPlayer.getVolume()*100)) + "%";
		sendTempMessage(msg);
	}


	/**
	 * Return the command-specific help String to BotHelp
	 */
	public String getHelp() {
		String helpMessage = "List of parameters for the '" + command + "' command:\n\n";

		if (command.equals("goto")) {
			helpMessage += "'goto *no_parameters*'\nThe bot will join the Voice Channel that you are currently in (Only works if you are in a Voice Channel)\n\n";
			helpMessage += "'goto *channel_name*'\nThe bot will join the specified Voice Channel **(case-sensitive)**";
		}
		else if (command.equals("exit")) {
			helpMessage += "'exit'\nThe bot will leave the Voice Channel it is currently in (Only works if it is currently in a Voice Channel)";
		}

		return helpMessage;
	}

}
