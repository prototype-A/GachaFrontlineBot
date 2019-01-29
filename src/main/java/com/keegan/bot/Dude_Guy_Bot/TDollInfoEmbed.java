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


public class TDollInfoEmbed extends CGScroll {

	private final boolean MOD3;
	private static final String URL_HEADER = "https://cdn.discordapp.com/attachments/487029209114345502/";

	public TDollInfoEmbed(IDiscordClient bot, IMessage msg, IUser usr, JSONObject cgJson, boolean mod3) {
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
		return oldEmbed.getThumbnail().getUrl();
	}

	protected void getNewEmbedFields(IEmbed oldEmbed, EmbedBuilder newEmbed) {
		Iterator<IEmbed.IEmbedField> fieldIter = oldEmbed.getEmbedFields().iterator();
		while (fieldIter.hasNext()) {
			newEmbed.appendField(fieldIter.next());
		}
	}

	protected String getNewFooterText(String[] newImage) {
		return newImage[0].replace(" (Live2D)", "") + (imgIndex + 1) + "/" + IMG_LIST.length;
	}

	protected String getNewImageUrl(String[] newImage) {
		return URL_HEADER + newImage[1] + ".png";
	}

}
