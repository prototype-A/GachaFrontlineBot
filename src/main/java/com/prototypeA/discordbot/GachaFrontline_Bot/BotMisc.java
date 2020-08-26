package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.Color;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class BotMisc extends Command {

	public BotMisc(String command) {
		this.command = command;
	}

	public void run() {

		// Check for permissions
		if (canIssueBotCommands()) {
			// Get the avatar url of the user's nickname
			if (command.equals("avatar")) {
				// User search query (either name or user id)
				User user = null;
				String search = getAsOneArg();
				String query = (search != null) ? search.toLowerCase() : null;

				// Display own avatar if no query provided
				if (query == null) {
					user = cmdMessage.getAuthor().get();
				} else {
					Guild guild = cmdMessage.getGuild().block();
					try {
						// Search by user ID
						user = guild.getMemberById(Snowflake.of(query)).block();
					} catch (Exception e1) {
						try {
							// Search by nickname and username + discriminator
							List<Member> foundList = guild.getMembers()
														.filter(member -> (member.getNickname().isPresent()) ?
																			member.getNickname().get().toLowerCase().contains(query) :
																			member.getDisplayName().toLowerCase().contains(query) ||
																			member.getTag().toLowerCase().contains(query))
														.buffer()
														.blockLast();
							user = foundList.get(0);
						} catch (Exception e) {}
					}
				}

				// Embed own avatar
				if (user == null) {
					sendTempMessage("User not found");
				} else {
					/* Local variables referenced from a
					lambda expressionMust be final or
					effectively final */
					final String title = user.getTag() + "'s Avatar";
					final String avatarUrl = getAvatarUrl(user) + "?size=2048";
					sendMessage(spec -> spec.setTitle(title)
										.setUrl(avatarUrl)
										.setImage(avatarUrl)
										.setColor(Color.of(Integer.parseInt(Main.getParameter("EmbedAvatarColor")))));
				}
			}

			// Quote a user's message from a channel that the bot can read
			else if (command.equals("quote")) {
				// Find message in current guild by message id
				String arg = getAsOneArg();
				Message msg = null;

				// Search all available text channels in guild for message
				List<GuildChannel> channels = cmdMessage.getGuild()
												.block()
												.getChannels()
												.buffer()
												.blockLast();
				Iterator<GuildChannel> chnIter = channels.iterator();
				while (chnIter.hasNext()) {
					try {
						TextChannel channel = (TextChannel)chnIter.next();
						msg = channel.getMessageById(Snowflake.of(arg)).block();
					} catch (Exception e) { /* Not a text channel */ }
				}

				if (msg == null) {
					sendTempMessage("Message not found");
				} else {
					final User msgAuthor = msg.getAuthor().get();
					final String title = msgAuthor.getTag();
					final String msgContent = msg.getContent();
					final String msgSentTime = msg.getTimestamp().toString();
					sendMessage(spec -> spec.setTitle(title)
										.setDescription(msgContent)
										.setFooter(msgSentTime, getAvatarUrl(msgAuthor))
										.setColor(Color.of(Integer.parseInt(Main.getParameter("EmbedQuoteColor")))));
				}
			}
		}
	}

	private String getAvatarUrl(User user) {
		return user.getAvatarUrl().replace(".webp", ".png");
	}

	/**
	 * Return the command-specific help String to BotHelp
	 */
	public String getHelp() {
		String helpMessage = "";
		if (command.equals("avatar")) {
			helpMessage += BotHelp.formatHelpMessage("avatar", "Displays your Discord profile picture");
			helpMessage += BotHelp.formatHelpMessage("avatar", "name", "Displays the Discord profile picture of the user with the specified nickname/username/username + tag");
			helpMessage += BotHelp.formatHelpMessage("avatar", "user_id", "Displays the Discord profile picture of the user with the specified user_id");
			helpMessage += BotHelp.formatHelpMessage("quote", "message_id", "Displays the message posted in a bot-accessible text channel within this server with the specified message_id");
		}

		return helpMessage;
	}

}
