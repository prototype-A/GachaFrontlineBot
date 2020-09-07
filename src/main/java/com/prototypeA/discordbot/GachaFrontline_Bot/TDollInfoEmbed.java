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
import java.util.Map;


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
			// Load skin CG
			JSONObject skinJson = IMG_JSON.getJSONObject("skins");
			JSONArray skinList = skinJson.getJSONArray("list");
			cgList = new String[defaultCG.length() + skinList.length() * 2];

			for (int i = defaultCG.length();  i < cgList.length; i = i + 2) {
				String skinName = (String)skinList.get((i - defaultCG.length()) / 2);
				cgList[i] = skinJson.getJSONObject(skinName).getString("normal");
				cgList[i + 1] = skinJson.getJSONObject(skinName).getString("damaged");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// No skins found
			cgList = new String[defaultCG.length()];
		} finally {
			// Load default CG
			JSONArray defaultCGList = defaultCG.toJSONArray(defaultCG.names());
			for (int i = 0; i < defaultCG.length(); i++) {
				cgList[i] = (String)defaultCGList.get(i);
			}
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
		return newImage[0] + "  " + (imgIndex + 1) + "/" + IMG_LIST.length;
	}

	protected String getNewImageUrl(String[] newImage) {
		return URL_HEADER + newImage[1] + ".png";
	}

	protected String[] getImage() {
		JSONObject defaultCG = IMG_JSON.getJSONObject("default");

		// Load skins, if any
		JSONObject skins = IMG_JSON.getJSONObject("skins");
		JSONArray skinList = null;
		try {
			skinList = skins.getJSONArray("list");
		} catch (Exception e) {}

		// Determine CG name (default or skin)
		String cgName = "";
		String expression = "";
		if (skinList != null) {
			// Determine cg name
			cgName = (imgIndex <= (defaultCG.length() - 1)) ? "Default" :
						(String)(skinList.get((imgIndex - defaultCG.length()) / 2));

			// Determine cg expression
			JSONArray cgExpr = defaultCG.names();
			if (imgIndex <= (cgExpr.length() - 1)) {
				// Default CG
				if (((String)(cgExpr.get(imgIndex))).startsWith("*")) {
					// Skin expression
					cgName = formatName(((String)(cgExpr.get(imgIndex))).replace("*", ""));
				}
				expression = (" (" +
							formatName(((String)(cgExpr.get(imgIndex))).replace("*", "")) +
							")");
			} else {
				// Skin CG condition (normal/damaged)
				JSONArray skinNames = skins.names();
				skins.names().remove(0);
				JSONArray skinUrlArray = skins.toJSONArray(skinNames);
				int skinIndex = (imgIndex - cgExpr.length()) / 2;
				JSONObject skinUrls = (JSONObject)skinUrlArray.get(skinIndex);
				int skinExprIndex = (imgIndex - cgExpr.length()) % 2;
				expression = (" (" +
							formatName((String)(skinUrls.names().get(skinExprIndex))) +
							")");
			}

			// Don't show cg name as expression if the same
			if (expression.equals(" (" + cgName + ")")) {
				expression = "";
			} else {
				expression = expression.replace(" (Normal)", "");
			}
		}


		return new String[]{ cgName + expression, IMG_LIST[imgIndex] };
	}
}
