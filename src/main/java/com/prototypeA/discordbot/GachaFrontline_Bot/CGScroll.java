package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.rest.util.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


public abstract class CGScroll extends BotEmbedMessage {

	private final LocalTime END_TIME;
	protected final JSONObject IMG_JSON;
	protected final String[] IMG_LIST;
	private static final String[] NAV_EMOJIS = {"⬅", "➡"};
	protected int imgIndex = 0;


	public CGScroll(GatewayDiscordClient bot, Message msg, User usr,
					JSONObject imgJson) {
		super(NAV_EMOJIS, bot, msg, usr);
		this.IMG_JSON = imgJson;
		this.IMG_LIST = buildImgList();
		this.END_TIME = LocalTime.now().plusSeconds(Integer.parseInt(Main.getParameter("CGScrollTime")) * IMG_LIST.length);

		// Timeout after x seconds
		CompletableFuture.runAsync(() -> {
			// Before time limit
			while (LocalTime.now().isBefore(END_TIME)) {
				try {
					this.sleep(1000);
				} catch (InterruptedException e1) {
					Main.displayWarning("CG Scroller sleep interrupted");
					e1.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			// After time limit
			try {
				this.msg.removeAllReactions().block();
				this.join();
			} catch (InterruptedException e1) {
				Main.displayWarning("Failed to join() CG Scroller");
				e1.printStackTrace();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		});

		this.GATEWAY.on(ReactionAddEvent.class).subscribe(event -> {
			if (event.getUser().block().getId().equals(this.USR.getId()) &&
				event.getMessageId().equals(this.msg.getId())) {
				String emoji = event.getEmoji().asUnicodeEmoji().get().getRaw();

				Embed currEmbed = this.msg.getEmbeds().get(0);
				if (emoji.equals("⬅")) {
					this.msg = this.msg.edit(msgSpec -> {
												msgSpec.setEmbed(rebuildEmbed(currEmbed, false));
											}).block();
				} else if (emoji.equals("➡")) {
					this.msg = this.msg.edit(msgSpec -> {
												msgSpec.setEmbed(rebuildEmbed(currEmbed, true));
											}).block();
				}
				redoReacts();
			}
		});
	}

	/**
	 * Returns a string array that contains the name of the cg
	 * as well as the image url of it
	 *
	 * @return A string array containing the skin name and the url of the cg
	 */
	protected abstract String[] buildImgList();

	/**
	 * Returns the title of the current (previous) embed
	 *
	 * @param oldEmbed The embed object
	 * @return The title of the old embed
	 */
	protected abstract String getNewTitle(Embed oldEmbed);

	/**
	 * Returns the url of the current (previous) embed
	 *
	 * @param oldEmbed The embed object
	 * @return The url of the old embed
	 */
	protected abstract String getNewUrl(Embed oldEmbed);

	/**
	 * Returns the description of the current (previous) embed
	 *
	 * @param oldEmbed The embed object
	 * @return The description of the old embed
	 */
	protected abstract String getNewDesc(Embed oldEmbed);

	/**
	 * Returns the color of the current (previous) embed
	 *
	 * @param oldEmbed The embed object
	 * @return The color of the old embed
	 */
	protected abstract Color getNewColor(Embed oldEmbed);

	/**
	 * Returns the thumbnail url of the current (previous) embed
	 *
	 * @param oldEmbed The embed object
	 * @return The thumbnail url of the old embed
	 */
	protected abstract String getNewThumbnailUrl(Embed oldEmbed);

	/**
	 * Appends the (additional) fields of the current (previous) embed
	 * to the new embed that will replace the current (previous) embed
	 *
	 * @param oldEmbed The embed object
	 * @param newEmbed The new embed builder
	 */
	protected abstract void getNewEmbedFields(Embed oldEmbed, EmbedCreateSpec newEmbed);

	/**
	 * Returns the updated footer text displaying the cg name
	 * and the updated index of the cg image in the gallery
	 *
	 * @param newImage The string array containing the new skin name
	 * @return The updated footer text
	 */
	protected abstract String getNewFooterText(String[] newImage);

	/**
	 * Returns the updated image url of the image in the gallery
	 *
	 * @param newImage The string array containing the new image url
	 * @return The new image to be embedded
	 */
	protected abstract String getNewImageUrl(String[] newImage);

	protected abstract String[] getImage();


	private Consumer<? super EmbedCreateSpec> rebuildEmbed(Embed oldEmbed,
															boolean next) {

		EmbedCreateSpec newEmbed = new EmbedCreateSpec();
		newEmbed = newEmbed.setTitle(getNewTitle(oldEmbed))
							.setUrl(getNewUrl(oldEmbed))
							.setDescription(getNewDesc(oldEmbed))
							.setColor(getNewColor(oldEmbed));

		getNewEmbedFields(oldEmbed, newEmbed);

		String[] newImage = (next) ? getNextImage() : getPrevImage();
		
		newEmbed = newEmbed.setThumbnail(getNewThumbnailUrl(oldEmbed))
							.setFooter(getNewFooterText(newImage), null)
							.setImage(getNewImageUrl(newImage));

		/* Local variable referenced from a lambda expression
		must be final or effectively final */
		EmbedData newEmbedData = newEmbed.asRequest();
		return spec -> {
			spec.setTitle(newEmbedData.title().get())
				.setUrl(newEmbedData.url().get())
				.setDescription(newEmbedData.description().get())
				.setColor(Color.of(newEmbedData.color().get()))
				.setThumbnail(newEmbedData.thumbnail().get().url().get())
				.setImage(newEmbedData.image().get().url().get())
				.setFooter(newEmbedData.footer().get().text(), null);

			Iterator<EmbedFieldData> fieldIter = newEmbedData.fields().get().iterator();
			while (fieldIter.hasNext()) {
				EmbedFieldData field = fieldIter.next();
				spec.addField(field.name(), field.value(), field.inline().get());
			}
		};
	}

	private String[] getPrevImage() {
		imgIndex = (imgIndex > 0) ? imgIndex - 1 : IMG_LIST.length - 1;
		return getImage();
	}

	private String[] getNextImage() {
		imgIndex = (imgIndex + 1) % IMG_LIST.length;
		return getImage();
	}

	protected void redoReacts() {
		this.msg.removeAllReactions().block();
		addEmojisToMessage(this.msg, NAV);
	}
}
