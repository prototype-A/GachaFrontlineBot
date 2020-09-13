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
import java.util.Map;


public class TDollInfoEmbed extends CGScroll {

	private final boolean MOD3;
	private static final String URL_HEADER = Main.getParameter("AssetHeader");


	public TDollInfoEmbed(GatewayDiscordClient bot, Message msg, User usr,
							JSONObject cgJson, boolean mod3) {
		super(bot, msg, usr, cgJson, false);
		this.MOD3 = mod3;
		this.IMG_LIST = buildImgList();
		buildImageList(false);
	}

	protected List<ImageCG> buildImgList() {

		List<ImageCG> cgList = new ArrayList<>();

		// Load default CG
		JSONObject defaultCG = IMG_JSON.getJSONObject("default");
		if (this.MOD3) {
			defaultCG = IMG_JSON.getJSONObject("mod3");
		}
		JSONArray defaultCGNames = defaultCG.names();
		JSONArray defaultCGList = defaultCG.toJSONArray(defaultCGNames);
		for (int i = 0; i < defaultCG.length(); i++) {
			String cgName = "Default";
			String cgExpression = (i == 0) ? "" :
									" (" + 
									formatName(defaultCGNames.getString(i)) +
									")";
			// Alternate CG
			if (defaultCGNames.getString(i).startsWith("*")) {
				cgName = formatName(defaultCGNames.getString(i).replace("*", ""));
				cgExpression = "";
			}

			cgList.add(new ImageCG(cgName + cgExpression, defaultCGList.getString(i)));
		}

		// Load skin CG
		try {
			JSONObject skinJson = IMG_JSON.getJSONObject("skins");
			JSONArray skinList = skinJson.getJSONArray("list");

			JSONArray skinNames = skinJson.names();
			for (int i = 0; i < skinNames.length(); i++) {
				// Remove skin list
				if (skinNames.getString(i).toLowerCase().equals("list")) {
					skinNames.remove(i);
					break;
				}
			}

			JSONArray skinUrlArray = skinJson.toJSONArray(skinNames);
			for (int i = 0; i < skinUrlArray.length() * 2; i++) {
				int skinIndex = i / 2;
				String skinName = skinNames.getString(skinIndex)
											.replace(" (Live2D)", "")
											.replace(" (Simple Live2D)", "");
				String skinCondition = (i % 2 == 0) ? "" : " (Damaged)";
				JSONObject skinUrls = skinUrlArray.getJSONObject(skinIndex);
				String skinUrl = skinUrls.toJSONArray(skinUrls.names())
											.getString(i % 2);

				cgList.add(new ImageCG(skinName + skinCondition, skinUrl));
			}
		} catch (Exception e) {
			// No skins found
			e.printStackTrace();
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

	protected String getNewFooterText(ImageCG newImage) {
		return newImage.getName() + "  " + (imgIndex + 1) + "/" + IMG_LIST.size();
	}

	protected String getNewImageUrl(ImageCG newImage) {
		return URL_HEADER + newImage.getImageUrl() + ".png";
	}

	protected ImageCG getImage() {
		return IMG_LIST.get(imgIndex);
	}
}
