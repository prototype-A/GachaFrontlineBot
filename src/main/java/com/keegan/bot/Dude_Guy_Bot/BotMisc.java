package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class BotMisc extends Command {

	public BotMisc(String command) {
		this.command = command;
	}

	public void run() {

		// Check for permissions
		if (canIssueBotCommands()) {

			// Get the avatar url of the user's nickname
			if (command.equals("avatar")) {

				// User search query
				String name = getAsOneArg();
				IUser user = null;

				// No username/nickname: Display own avatar
				if (name == null) {
					user = cmdMessage.getAuthor();
				} else {
					try {
						// Search by user ID
						user = guild.getUserByID(Long.valueOf(name));
					} catch (Exception e1) {
						try {
							// Search by name
							List<IUser> foundList = guild.getUsersByName(name, true);
							user = foundList.get(0);
						} catch (Exception e2) {}
					}
				}
				// Embed own avatar
				if (user == null) {
					sendTempMessage("User not found");
				} else {
					String avatarUrl = getAvatarUrl(user) + "?size=2048";
					EmbedBuilder avatarEmbed = new EmbedBuilder();
					avatarEmbed.withTitle(user.getName() + "#" + user.getDiscriminator() + "'s Avatar");
					avatarEmbed.withUrl(avatarUrl);
					avatarEmbed.withImage(avatarUrl);
					avatarEmbed.withColor(Integer.parseInt(Main.getParameter("EmbedAvatarColor")));
					sendMessage(avatarEmbed.build());
				}
			}

			// Quote a user's message from a channel that the bot can read
			else if (command.equals("quote")) {

				// Find message in current guild by message id
				String arg = getAsOneArg();

				IMessage msg = null;
				try {
					// Channel ID provided
					if (arg.contains("/")) {
						String[] ids = arg.split("/");
						long chnId = Long.valueOf(ids[0]);
						long msgId = Long.valueOf(ids[1]);
						msg = getChannel(chnId).fetchMessage(msgId);
					} else {
						msg = cmdMessage.getChannel().fetchMessage(Long.valueOf(arg));
					}

					if (msg == null) {
						sendTempMessage("Message not found");
					} else {
						IUser msgAuthor = msg.getAuthor();
						sendMessage(new EmbedBuilder()
							.withTitle(msgAuthor.getName() + "#" + 
								msgAuthor.getDiscriminator())
							.withDesc(msg.getContent())
							.withFooterIcon(getAvatarUrl(msgAuthor))
							.withFooterText(msg.getTimestamp().toString())
							.withColor(Integer.parseInt(Main.getParameter("EmbedQuoteColor"))).build());
					}
				} catch (Exception e) {
					sendTempMessage("Invalid usage");
				}
			}

			//
			else if (command.equals("")) {
				
			}

		}

	}

	private String getAvatarUrl(IUser user) {
		return user.getAvatarURL().replace(".webp", ".png");
	}

	/**
	 * Return the command-specific help String to BotHelp
	 */
	public String getHelp() {
		String helpMessage = "";
		if (command.equals("avatar")) {
			helpMessage += formatHelpMessage("avatar", "Gets own Discord profile picture");
			helpMessage += formatHelpMessage("avatar", "user", "Gets the Discord profile picture of the specified user (case-sensitive)");
			helpMessage += formatHelpMessage("avatar", "user_id", "Gets the Discord profile picture of the user with user_id");
		}

		return helpMessage;
	}

}
