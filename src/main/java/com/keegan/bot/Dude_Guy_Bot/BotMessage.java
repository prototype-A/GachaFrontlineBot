package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RateLimitException;


public abstract class BotMessage {

	protected IMessage cmdMessage;
	protected IGuild guild;
	protected IDiscordClient botClient;


	protected void init(IMessage message, IDiscordClient client) {
		cmdMessage = message;
		guild = message.getGuild();
		botClient = client;
	}

	/**
	 * Constructs and sends a message to the same channel that 
	 * the command was passed to
	 *
	 * @param content The message string to send
	 * @return The sent message
	 */
	protected IMessage sendMessage(String content) {
		IMessage newMessage = null;
		MessageBuilder msgBuilder = new MessageBuilder(botClient).withChannel(cmdMessage.getChannel()).withContent(content);

		try {
			newMessage = msgBuilder.build();
		} catch (Exception e) {
			Main.displayError("Error sending message: \"" + content + "\"", e);
		} finally {
			return newMessage;
		}
	}

	/**
	 * Constructs and sends a message to the same channel that the command 
	 * was passed to, also attaching a list of emojis as reactiongs in 
	 * their sent order
	 *
	 * @param content The message string to send
	 * @param emojis A list of emojis to add to the message as reactions
	 * @return The sent message
	 */
	protected IMessage sendMessage(String content, String[] emojis) {
		IMessage newMessage = null;
		try {
			newMessage = sendMessage(content);
			addEmojisToMessage(newMessage, emojis);
		} catch (Exception e) {
			Main.displayError("Error sending message: \"" + content + "\", " + e.getMessage() + " occurred", e);
		} finally {
			return newMessage;
		}
	}

	/**
	 * Constructs and sends embedded content to the same channel
	 * that the command was passed to
	 *
	 * @param embedContent 
	 */
	protected IMessage sendMessage(EmbedObject embedContent) {
		IMessage newMessage = null;
		try {
			newMessage = cmdMessage.getChannel().sendMessage(embedContent);
		} catch (Exception e) {
			Main.displayError("Error sending message: " + e.getMessage() + " occurred", e);
		} finally {
			return newMessage;
		}
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
		IMessage newMessage = null;

		try {
			newMessage = sendMessage(content);
			delay(Integer.parseInt(Main.getParameter("TempMessageTime")) * 1000);
			newMessage.delete();
			cmdMessage.delete();
		} catch (Exception e) {
			Main.displayError(e.getMessage() + " occurred while attempting to delete a temporary message", e);
		}
	}

	/**
	 * Try to add emojis as reactions to the message asap, retrying
	 * after getting a RateLimitException and waiting out its delay
	 * period so all reactions will be added eventually
	 *
	 * @param message The message to add emojis to
	 * @param emoji The emoji to add
	 */
	protected void addEmojisToMessage(IMessage message, String[] emojis) {
		for (int emoji = 0; emoji < emojis.length; emoji++) {
			try {
				addEmojiToMessage(message, emojis[emoji]);
			} catch (RateLimitException e) {
				delay(e.getRetryDelay() + 1);
				addEmojiToMessage(message, emojis[emoji]);
			} catch (Exception e) {
				Main.displayError(e.getMessage() + " occurred while adding a reaction", e);
			}
		}
	}

	/**
	 * Try to add emojis as reactions to the message asap, retrying
	 * after getting a RateLimitException and waiting out its delay
	 * period so all reactions will be added eventually
	 *
	 * @param message The message to add emojis to
	 * @param emoji The emoji to add
	 */
	private void addEmojiToMessage(IMessage message, String emoji) {
		message.addReaction(ReactionEmoji.of(emoji));
	}

	protected abstract void redoReacts();

	/**
	 * Wait for delayPeriod milliseconds before resuming
	 */
	protected void delay(long period) {
		try {
			Thread.sleep(period);
		} catch (InterruptedException e) {
			Main.displayWarning("Thread sleep interrupted: Continuing execution");
		} catch (IllegalArgumentException e) {
			//Main.displayError("Negative timeout value occurred: Trying again");
			delay(period);
		}
	}
}
