package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;

import java.util.function.Consumer;


public abstract class Messaging {

	protected GatewayDiscordClient gateway;
	protected Message commandMessage;


	protected void init(GatewayDiscordClient gateway, Message message) {
		this.gateway = gateway;
		this.commandMessage = message;
	}

	/**
	 * Deletes the original message sent by the user to 
	 * invoke a bot command
	 */
	protected void deleteCommandMessage() {
		commandMessage.delete().subscribe();
	}

	/**
	 * Constructs and sends a message to the same channel that 
	 * the command was passed to
	 *
	 * @param content The message string to send
	 */
	protected Message sendMessage(String content) {
		return commandMessage.getChannel()
								.block()
								.createMessage(content)
								.block();
	}

	/**
	 * Constructs and sends a message to the same channel that the command 
	 * was passed to, also attaching a list of emojis as reactiongs in 
	 * their sent order
	 *
	 * @param content The message string to send
	 * @param emojis A list of emojis to add to the message as reactions
	 */
	protected Message sendMessage(String content, String[] emojis) {
		Message newMessage = commandMessage.getChannel()
											.block()
											.createMessage(content)
											.block();
		addEmojisToMessage(newMessage, emojis);

		return newMessage;
	}

	/**
	 * Constructs and sends embedded content to the same channel
	 * that the command was passed to
	 *
	 * @param embedSpec The embedded content to send
	 */
	protected Message sendMessage(Consumer<? super LegacyEmbedCreateSpec> embeds) {
		return commandMessage.getChannel()
								.block()
								.createEmbed(embeds)
								.block();
	}

	/**
	 * Sends an embed as a message with up to 5 rows of buttons
	 *
	 * @param embed The embed to send in the message
	 * @param buttonRows The ActionRows of buttons to attach on the message
	 * @return The sent message
	 */
	protected Message sendMessage(EmbedCreateSpec embed, ActionRow... buttonRows) {
		if (buttonRows.length > 5) {
			return null;
		}

		return commandMessage.getChannel()
				.block()
				.createMessage(MessageCreateSpec.create()
					.withEmbeds(embed)
					.withComponents(buttonRows))
				.block();
	}

	/**
	 * Constructs and sends a temporary message to the channel 
	 * that the command was passed to. It will be deleted after 
	 * a short delay, along with the user message that issued the
	 * command
	 *
	 * @param content The message string to send
	 */
	protected void sendTempMessage(String content) {
		Message newMessage = commandMessage.getChannel()
											.block()
											.createMessage(content)
											.block();
		delay(Integer.parseInt(Main.getParameter("TempMessageTime")) * 1000);
		newMessage.delete().subscribe();
		deleteCommandMessage();
	}

	/**
	 * Constructs and sends a temporary message to the channel 
	 * that the command was passed to. It will be deleted after 
	 * a short delay, along with the user message that issued the
	 * command
	 *
	 * @param embeds The embedded content to send
	 */
	protected void sendTempMessage(Consumer<? super LegacyEmbedCreateSpec> embeds) {
		Message newMessage = commandMessage.getChannel()
											.block()
											.createEmbed(embeds)
											.block();
		delay(Integer.parseInt(Main.getParameter("TempMessageTime")) * 1000);
		newMessage.delete().subscribe();
		deleteCommandMessage();
	}

	/**
	 * Constructs and sends a direct message to the
	 * specified user
	 *
	 * @param content The message string to send
	 * @param user The user to send the direct message to
	 */
	protected Message sendDirectMessage(String content, User user) {
		return user.getPrivateChannel()
					.block()
					.createMessage(content)
					.block();
	}

	/**
	 * Constructs and sends embedded content directly to the
	 * specified user
	 *
	 * @param embed The embed to send
	 * @param user The user to send the direct message to
	 */
	protected Message sendDirectMessage(Consumer<? super LegacyEmbedCreateSpec> embeds, User user) {
		return user.getPrivateChannel()
					.block()
					.createEmbed(embeds)
					.block();
	}

	/**
	 * Try to add a list of emojis as reactions to the message
	 *
	 * @param message The message to add emojis to
	 * @param emojis The list of emojis to react with
	 */
	protected void addEmojisToMessage(Message message, String[] emojis) {
		for (int emoji = 0; emoji < emojis.length; emoji++) {
			addEmojiToMessage(message, emojis[emoji]);
		}
	}

	/**
	 * Adds a unicode emoji reaction to the message
	 *
	 * @param message The message to add an emoji to
	 * @param emoji The emoji to react with
	 */
	private void addEmojiToMessage(Message message, String emoji) {
		message.addReaction(ReactionEmoji.unicode(emoji)).block();
	}

	/**
	 * Capitalizes all words and replaces underscores
	 * with spaces
	 *
	 * @param name The string to format
	 * @return The formatted string
	 *
	protected String formatName(String name) {
		String[] words = name.split("_");
		String formattedName = "";
		for (int i = 0; i < words.length; i++) {
			formattedName += Character.toTitleCase(words[i].charAt(0)) +
								words[i].substring(1) + " ";
		}

		return formattedName.trim();
	}*/

	//protected abstract void redoReacts();


	/**
	 * Wait for delayPeriod milliseconds before resuming
	 */
	protected void delay(long period) {
		try {
			Thread.sleep(period);
		} catch (InterruptedException e) {
			//Main.displayWarning("Thread sleep interrupted: Continuing execution");
		} catch (IllegalArgumentException e) {
			//Main.displayError("Negative timeout value occurred: Trying again");
			delay(period);
		} catch (Exception e) {
			ConsoleUtils.printWarning("Error while sleeping thread: " + e.getMessage());
		}
	}
}
