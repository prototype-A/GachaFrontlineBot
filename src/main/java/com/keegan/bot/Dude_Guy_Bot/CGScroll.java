package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RateLimitException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;


public abstract class CGScroll extends Thread {

	private final IUser USR;
	private IMessage msg;
	private final LocalTime END_TIME;
	protected final JSONObject IMG_JSON;
	protected final String[] IMG_LIST;
	private static final String[] NAV = {"⬅", "➡"};
	protected int imgIndex = 0;


	public CGScroll(IUser usr, IMessage msg, IDiscordClient bot, JSONObject imgJson) {
		this.USR = usr;
		this.msg = msg;
		this.IMG_JSON = imgJson;
		this.IMG_LIST = buildImgList();
		this.END_TIME = LocalTime.now().plusSeconds(Integer.parseInt(Main.getParameter("CGScrollTime")) * IMG_LIST.length);

		bot.getDispatcher().registerListener(this);
		addEmojisToMessage(this.msg, NAV);

		// Timeout after x seconds
		CompletableFuture.runAsync(() -> {
			// Before time limit
			while (LocalTime.now().isBefore(END_TIME)) {
				try {
					this.sleep(1000);
				} catch (Exception e) {}
			}
			// After time limit
			this.msg.removeAllReactions();
			try {
				this.join();
			} catch (Exception e) {}
		});
	}

	protected abstract String[] buildImgList();
	protected abstract String getNewTitle(IEmbed oldEmbed);
	protected abstract String getNewUrl(IEmbed oldEmbed);
	protected abstract String getNewDesc(IEmbed oldEmbed);
	protected abstract Color getNewColor(IEmbed oldEmbed);
	protected abstract String getNewThumbnailUrl(IEmbed oldEmbed);
	protected abstract void getNewEmbedFields(IEmbed oldEmbed, EmbedBuilder newEmbed);
	protected abstract String getNewFooterText(String[] newImage);
	protected abstract String getNewImageUrl(String[] newImage);
	protected abstract String[] getImage();


	private EmbedObject rebuildEmbed(IEmbed oldEmbed, boolean next) {

		EmbedBuilder newEmbed = new EmbedBuilder();

		newEmbed.withTitle(getNewTitle(oldEmbed));
		newEmbed.withUrl(getNewUrl(oldEmbed));
		newEmbed.withDesc(getNewDesc(oldEmbed));
		newEmbed.withColor(getNewColor(oldEmbed));
		newEmbed.withThumbnail(getNewThumbnailUrl(oldEmbed));

		getNewEmbedFields(oldEmbed, newEmbed);

		String[] newImage = (next) ? getNextImage() : getPrevImage();

		newEmbed.withFooterText(getNewFooterText(newImage));
		newEmbed.withImage(getNewImageUrl(newImage));

		return newEmbed.build();
	}

	private String[] getPrevImage() {
		imgIndex = (imgIndex > 0) ? imgIndex - 1 : IMG_LIST.length - 1;
		return getImage();
	}

	private String[] getNextImage() {
		imgIndex = (imgIndex + 1) % IMG_LIST.length;
		return getImage();
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

	/**
	 * Wait for delayPeriod milliseconds before resuming
	 */
	private void delay(long period) {
		try {
			Thread.sleep(period);
		} catch (InterruptedException e) {
			Main.displayWarning("CG Scrolling thread sleep interrupted: Continuing execution");
		} catch (IllegalArgumentException e) {
			//Main.displayError("Negative timeout value occurred: Trying again");
			delay(period);
		}
	}

	private void redoReacts() {
		this.msg.removeAllReactions();
		addEmojisToMessage(this.msg, NAV);
	}

	@EventSubscriber
	public void changeCG(ReactionAddEvent event) {
		if (event.getUser() == this.USR && event.getMessageID() == this.msg.getLongID()) {
			String emoji = event.getReaction().getEmoji().toString();
			try {
				if (emoji.equals("⬅")) {
					this.msg = this.msg.edit(rebuildEmbed(this.msg.getEmbeds().get(0), false));
				} else if (emoji.equals("➡")) {
					this.msg = this.msg.edit(rebuildEmbed(this.msg.getEmbeds().get(0), true));
				}
				redoReacts();
			} catch (RateLimitException e) {
				delay(e.getRetryDelay());
				changeCG(event);
			}
		}
	}
}
