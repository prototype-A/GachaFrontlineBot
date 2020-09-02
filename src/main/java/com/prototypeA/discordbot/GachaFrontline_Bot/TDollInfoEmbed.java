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


public class TDollInfoEmbed extends CGScroll {

	private final boolean MOD3;
	private static final String URL_HEADER = Main.getParameter("AssetHeader");


	public TDollInfoEmbed(GatewayDiscordClient bot, Message msg, User usr,
							JSONObject cgJson, boolean mod3) {
		super(bot, msg, usr, cgJson);
		this.MOD3 = mod3;
	}

	protected String[] buildImgList() {
		JSONObject defaultCG = IMG_JSON.getJSONObject("default");
		if (MOD3) {
			defaultCG = IMG_JSON.getJSONObject("mod3");
		}
		String[] cgList = null;
		try {
			JSONObject skinJson = IMG_JSON.getJSONObject("skins");
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
		return oldEmbed.getThumbnail().get().getUrl();
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
		return newImage[0].replace(" (Live2D)", "") + (imgIndex + 1) + "/" + IMG_LIST.length;
	}

	protected String getNewImageUrl(String[] newImage) {
		return URL_HEADER + newImage[1] + ".png";
	}

	protected String[] getImage() {
		JSONArray skinList = null;
		try {
			skinList = IMG_JSON.getJSONObject("skins").getJSONArray("list");
		} catch (Exception e) {}

		String cgName = "Default";
		if (skinList != null) {
			cgName = (imgIndex <= 1) ? "Default" :
						(String)(skinList.get(imgIndex / 2 - 1));
		}
		String cgCond = (imgIndex % 2 == 0) ? " " : " (Damaged) ";

		return new String[]{ cgName + cgCond, IMG_LIST[imgIndex] };
	}
}
