package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.function.Consumer;


public abstract class BotMessage extends Thread {

	protected Message cmdMessage;
	protected GatewayDiscordClient gateway;


	protected void init(Message message, GatewayDiscordClient gateway) {
		cmdMessage = message;
		this.gateway = gateway;
	}

	/**
	 * Constructs and sends a message to the same channel that 
	 * the command was passed to
	 *
	 * @param content The message string to send
	 */
	protected void sendMessage(String content) {
		Message newMessage = cmdMessage.getChannel()
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
	protected void sendMessage(String content, String[] emojis) {
		Message newMessage = cmdMessage.getChannel()
								.block()
								.createMessage(content)
								.block();
		addEmojisToMessage(newMessage, emojis);
	}

	/**
	 * Constructs and sends embedded content to the same channel
	 * that the command was passed to
	 *
	 * @param embed The embed to send
	 */
	protected void sendMessage(Consumer <? super EmbedCreateSpec> embedSpec) {
		Message newMessage = cmdMessage.getChannel()
							.block()
							.createEmbed(embedSpec)
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
		Message newMessage = cmdMessage.getChannel()
								.block()
								.createMessage(content)
								.block();
		delay(Integer.parseInt(Main.getParameter("TempMessageTime")) * 1000);
		newMessage.delete().block();
		cmdMessage.delete().block();
	}

	/**
	 * Constructs and sends a direct message to the
	 * specified user
	 *
	 * @param content The message string to send
	 * @param user The user to send the direct message to
	 */
	protected void sendDirectMessage(String content, User user) {
		Message newMessage = user.getPrivateChannel()
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
	protected void sendDirectMessage(Consumer <? super EmbedCreateSpec> embedSpec, User user) {
		Message newMessage = user.getPrivateChannel()
								.block()
								.createEmbed(embedSpec)
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
		message.addReaction(ReactionEmoji.of(null, emoji, false));
	}

	protected abstract void redoReacts();

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
			Main.displayWarning("Error while sleeping thread: " + e.getMessage());
		}
	}
}
