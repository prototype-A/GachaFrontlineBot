package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.util.Iterator;


public class FairyInfoEmbed extends CGScroll {

	private static final String URL_HEADER = "https://cdn.discordapp.com/attachments/487029209114345502/";

	public FairyInfoEmbed(IDiscordClient bot, IMessage msg, IUser usr, JSONObject cgJson) {
		super(bot, msg, usr, cgJson);
	}

	protected String[] buildImgList() {
		String[] cgList = new String[3];
		for (int i = 0; i < cgList.length; i++) {
			cgList[i] = IMG_JSON.getString("" + (i + 1));
		}

		return cgList;
	}

	protected String[] getImage() {
		return new String[]{ new String(new char[imgIndex]).replace("\0", "**") + "* ", IMG_LIST[imgIndex] };
	}

	protected String getNewTitle(IEmbed oldEmbed) {
		return oldEmbed.getTitle();
	}

	protected String getNewUrl(IEmbed oldEmbed) {
		return oldEmbed.getUrl();
	}

	protected String getNewDesc(IEmbed oldEmbed) {
		return oldEmbed.getDescription();
	}

	protected Color getNewColor(IEmbed oldEmbed) {
		return oldEmbed.getColor();
	}

	protected String getNewThumbnailUrl(IEmbed oldEmbed) {
		return URL_HEADER + IMG_JSON.getString("" + (imgIndex + 1) + "" + (imgIndex + 1)) + ".png";
	}

	protected void getNewEmbedFields(IEmbed oldEmbed, EmbedBuilder newEmbed) {
		Iterator<IEmbed.IEmbedField> fieldIter = oldEmbed.getEmbedFields().iterator();
		while (fieldIter.hasNext()) {
			newEmbed.appendField(fieldIter.next());
		}
	}

	protected String getNewFooterText(String[] newImage) {
		return newImage[0] + (imgIndex + 1) + "/" + IMG_LIST.length;
	}

	protected String getNewImageUrl(String[] newImage) {
		return URL_HEADER + newImage[1] + ".png";
	}

}
