package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class FairyInfoEmbed extends CGScroll {

	private static final String URL_HEADER = Main.getParameter("AssetHeader");

	public FairyInfoEmbed(GatewayDiscordClient bot, Message msg, User usr,
							JSONObject cgJson) {
		super(bot, msg, usr, cgJson, true);
	}

	protected List<ImageCG> buildImgList() {
		List<ImageCG> cgList = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			String rating = "*";
			for (int j = 0; j < i - 1; j++) {
				rating += "**";
			}
			cgList.add(new ImageCG(rating, IMG_JSON.getString("" + i)));
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

	protected String getNewFooterText(ImageCG newImage) {
		return newImage.getName() + " " + (imgIndex + 1) + "/" + IMG_LIST.size();
	}

	protected String getNewImageUrl(ImageCG newImage) {
		return URL_HEADER + newImage.getImageUrl() + ".png";
	}

	protected ImageCG getImage() {
		return IMG_LIST.get(imgIndex);
	}
}
