package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;


public class FairyInfoEmbed extends CGScroll {

	private static final String URL_HEADER = Main.getParameter("AssetHeader");

	public FairyInfoEmbed(GatewayDiscordClient bot, Message msg, User usr,
							JSONObject cgJson) {
		super(bot, msg, usr, cgJson);
	}

	protected String[] buildImgList() {
		String[] cgList = new String[3];
		for (int i = 0; i < cgList.length; i++) {
			cgList[i] = IMG_JSON.getString("" + (i + 1));
		}

		return cgList;
	}

	protected String getNewTitle(Embed oldEmbed) {
		return oldEmbed.getTitle().get();
	}

	protected String getNewUrl(Embed oldEmbed) {
		return oldEmbed.getUrl().get();
	}

	protected String getNewDesc(Embed oldEmbed) {
		return oldEmbed.getDescription().get();
	}

	protected Color getNewColor(Embed oldEmbed) {
		return oldEmbed.getColor().get();
	}

	protected String getNewThumbnailUrl(Embed oldEmbed) {
		return URL_HEADER + IMG_JSON.getString("" + (imgIndex + 1) + "" +
				(imgIndex + 1)) + ".png";
	}

	protected void getNewEmbedFields(Embed oldEmbed, EmbedCreateSpec newEmbed) {
		Iterator<Embed.Field> fieldIter = oldEmbed.getFields().iterator();
		while (fieldIter.hasNext()) {
			Embed.Field field = fieldIter.next();
			newEmbed = newEmbed.addField(field.getName(), field.getValue(),
											field.isInline());
		}
	}

	protected String getNewFooterText(String[] newImage) {
		return newImage[0] + (imgIndex + 1) + "/" + IMG_LIST.length;
	}

	protected String getNewImageUrl(String[] newImage) {
		return URL_HEADER + newImage[1] + ".png";
	}

	protected String[] getImage() {
		return new String[]{ new String(new char[imgIndex]).replace("\0", "**") + "* ",
								IMG_LIST[imgIndex] };
	}
}
