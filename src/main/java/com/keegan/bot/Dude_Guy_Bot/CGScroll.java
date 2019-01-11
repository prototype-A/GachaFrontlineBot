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
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;


public class CGScroll extends Thread {

	private final IUser USR;
	private IMessage msg;
	private final boolean MOD3;
	private final LocalTime END_TIME;
	private static final String[] NAV = {"⬅", "➡"};
	private final JSONObject CG_JSON;
	private static final String URL_HEADER = "https://cdn.discordapp.com/attachments/487029209114345502/";
	private final String[] CG_LIST;
	private int cgIndex = 0;

	public CGScroll(IUser usr, IMessage msg, IDiscordClient bot, JSONObject cgJson, boolean mod3) {
		this.USR = usr;
		this.msg = msg;
		this.CG_JSON = cgJson;
		this.MOD3 = mod3;
		this.CG_LIST = buildCGList();
		this.END_TIME = LocalTime.now().plusSeconds(Integer.parseInt(Main.getParameter("CGScrollTime")) * CG_LIST.length);

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

	private String[] buildCGList() {
		JSONObject defaultCG = CG_JSON.getJSONObject("default");
		if (MOD3) {
			defaultCG = CG_JSON.getJSONObject("mod3");
		}
		String[] cgList = null;
		try {
			JSONObject skinJson = CG_JSON.getJSONObject("skins");
			JSONArray skinList = skinJson.getJSONArray("list");
			cgList = new String[2 + skinList.length() * 2];

			for (int i = 2;  i < cgList.length; i = i + 2) {
				String skin = (String)skinList.get(i / 2 - 1);
				cgList[i] = skinJson.getJSONObject(skin).getString("normal");
				cgList[i + 1] = skinJson.getJSONObject(skin).getString("damaged");
			}
		} catch (Exception e) {
			// No skins found
			cgList = new String[2];
		} finally {
			// Load default CG
			cgList[0] = defaultCG.getString("normal");
			cgList[1] = defaultCG.getString("damaged");
		}

		return cgList;
	}

	private EmbedObject rebuildEmbed(IEmbed oldEmbed, boolean next) {

		EmbedBuilder newEmbed = new EmbedBuilder();

		newEmbed.withTitle(oldEmbed.getTitle());
		newEmbed.withUrl(oldEmbed.getUrl());
		newEmbed.withDesc(oldEmbed.getDescription());
		newEmbed.withColor(oldEmbed.getColor());
		newEmbed.withThumbnail(oldEmbed.getThumbnail().getUrl());

		Iterator<IEmbed.IEmbedField> fieldIter = oldEmbed.getEmbedFields().iterator();
		while (fieldIter.hasNext()) {
			newEmbed.appendField(fieldIter.next());
		}

		String[] newCG = (next) ? getNextCG() : getPrevCG();
		newEmbed.withFooterText(newCG[0].replace(" (Live2D)", "") + (cgIndex + 1) + "/" + CG_LIST.length);
		newEmbed.withImage(URL_HEADER + newCG[1] + ".png");

		/*
		if (oldEmbed.getFooter().getText().equals("Normal 1/2")) {
			newEmbed.withImage(CG_URL2);
			newEmbed.withFooterText("Damaged 2/2");
		} else {
			newEmbed.withImage(CG_URL1);
			newEmbed.withFooterText("Normal 1/2");
		}
		*/

		return newEmbed.build();
	}

	private String[] getCG() {
		JSONArray skinList = null;
		try {
			skinList = CG_JSON.getJSONObject("skins").getJSONArray("list");
		} catch (Exception e) {}

		String cgName = "Default";
		if (skinList != null) {
			cgName = (cgIndex <= 1) ? "Default" :
						(String)(skinList.get(cgIndex / 2 - 1));
		}
		String cgCond = (cgIndex % 2 == 0) ? " " : " (Damaged) ";

		return new String[]{ cgName + cgCond, CG_LIST[cgIndex] };
	}

	private String[] getPrevCG() {
		cgIndex = (cgIndex > 0) ? cgIndex - 1 : CG_LIST.length - 1;
		return getCG();
	}

	private String[] getNextCG() {
		cgIndex = (cgIndex + 1) % CG_LIST.length;
		return getCG();
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
