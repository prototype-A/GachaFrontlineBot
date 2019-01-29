package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class BotGF extends Command {

	private static JSONObject tdollDataJson;
	private static JSONObject timerDataJson;
	//private JSONObject equipDataJson;
	private static JSONObject mapDataJson;


	public BotGF(String command) {
		this.command = command;
		loadJson();
	}

	public void run() {
		
		// Check for permissions
		if (canIssueBotCommands()) {

			String arg = null;
			try {
				arg = getAsOneArg().replaceAll("\\s", "").replace(".", "").toLowerCase();
			} catch (Exception e) {}

			if (this.command.equals("tdoll")) {
				arg = arg.replace("-", "");
				boolean mod3 = false;
				if (arg.length() >= 5) {
					if (arg.substring(arg.length() - 4, arg.length()).toLowerCase().equals("mod3")) {
						mod3 = true;
						arg = arg.substring(0, arg.length() - 4);
					}
				}
				IMessage msg = sendMessage(displayTdollInfo(arg, mod3));
				try {
					JSONObject cgJson = getTdollData(arg).getJSONObject("cg");
					CGScroll cgScroller = new TDollInfoEmbed(botClient, msg, cmdMessage.getAuthor(), cgJson, mod3);
					cgScroller.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (this.command.equals("equip")) {
				//sendMessage(displayEquipmentInfo(arg));
			} else if (this.command.equals("timer")) {
				arg = arg.replace("-", "");
				JsonEmbed.embedAsWebhook(Main.getParameter("TimerWebhook"), getTdollsFromTimer(arg));
			} else if (this.command.equals("map")) {
				JsonEmbed.embedAsWebhook(Main.getParameter("MapWebhook"), displayMapInfo(arg));
			}
		}
	}

	private JsonEmbed.EmbedJsonStringBuilder getTdollsFromTimer(String timer) {

		// Parse the timer given
		int colonCount = 0;
		for (int i = 0; i < timer.length(); i++) {
			if (timer.charAt(i) == ':') {
				colonCount++;
			}
		}
		if (colonCount == 0) {
			// e.g. "215"
			timer = timer.charAt(0) + ":" + timer.substring(1) + ":00";
		} else if (colonCount == 1) {
			// e.g. "2:15"
			timer += ":00";
		} else if (colonCount > 2) {
			return JsonEmbed.errorEmbedJson("Invalid timer");
		}

		// Get possible T-dolls
		try {
			return JsonEmbed.successEmbedJson("**Possible T-Dolls:** " + timerDataJson.getString(timer));
		} catch (Exception e) {}

		// No T-dolls with timer
		return JsonEmbed.errorEmbedJson("**There are no T-Dolls with that Production time**");
	}

	private JSONObject getMapData(String name) {
		if (name.contains("operation")) {
			name = name.replace("operation", "");
		}

		// Check if alias
		try {
			return mapDataJson.getJSONObject(mapDataJson.getJSONObject(name).getString("data"));
		} catch (Exception e) {}

		return mapDataJson.getJSONObject(name);
	}

	private JsonEmbed.EmbedJsonStringBuilder displayMapInfo(String name) {
		// Parse given args
		try {
			int dashPos = name.indexOf("-");
			String mapName = name.substring(0, dashPos - 1);
			String map = name.substring(dashPos - 1, dashPos + 2);

			// Build embed
			JSONObject opData = getMapData(mapName);
			JSONObject mapData = opData.getJSONObject(map);
			String URL_HEADER = "https://cdn.discordapp.com/attachments/487029209114345502/";
			JsonEmbed.EmbedJsonStringBuilder mapInfo = new JsonEmbed.EmbedJsonStringBuilder();

			mapInfo.withTitle(mapData.getString("title"));
			mapInfo.withDesc(opData.getString("name"));
			mapInfo.withColor(Main.getParameter("EmbedSuccessColor"));
			mapInfo.withImage(URL_HEADER + mapData.getString("map") + ".png");

			// Display Day/Night battle
			mapInfo.appendField("Type", mapData.getString("type") + " Battle", true);

			// Display # of runs to complete
			mapInfo.appendField("Runs to Clear", mapData.getString("runs"), true);

			// Display clear objective
			mapInfo.appendField("Objective", mapData.getString("objective"), false);

			// Display map clear reward
			mapInfo.appendField("Clear Reward", mapData.getString("clear_reward"), false);

			// Display map limited drops
			try {
				mapInfo.appendField("Limited Drops", mapData.getString("limited_drops"), false);
			} catch (Exception e) {}

			return mapInfo;
		} catch (Exception e) {
			Main.displayError("Failed to embed map info");
			e.printStackTrace();
		}

		// Map not found
		return JsonEmbed.errorEmbedJson("**Map not found**");
	}

	private JSONObject getTdollData(String name) {
		// Check if nickname
		try {
			return tdollDataJson.getJSONObject(tdollDataJson.getJSONObject(name).getString("data"));
		} catch (Exception e) {}

		return tdollDataJson.getJSONObject(name);
	}

	private EmbedObject displayTdollInfo(String name, boolean mod3) {

		String URL_HEADER = "https://cdn.discordapp.com/attachments/487029209114345502/";

		EmbedBuilder infoPanel = new EmbedBuilder();
		JSONObject tdollData = null;
		JSONObject data = null;
		JSONObject cg = null;

		try {
			tdollData = getTdollData(name);

			// Get mod3 data
			data = (mod3) ? tdollData.getJSONObject("mod3") : tdollData.getJSONObject("");
			cg = tdollData.getJSONObject("cg");
		} catch (Exception e) {
			// No mod3 data
			if (mod3) {
				return JsonEmbed.errorEmbed("That T-Doll's Digimind Upgrade data was not found in the database");
			}

			return JsonEmbed.errorEmbed("That T-Doll's data was not found in the database");
		}

		// Title
		String tdollName = data.getString("name");
		infoPanel.withTitle(tdollName);

		// Link title to gfwiki page
		tdollName = tdollName.replace(" ", "_");
		if (mod3) {
			tdollName = tdollName.substring(0, tdollName.lastIndexOf("_")) + "#Mod3";
			infoPanel.withUrl("https://en.gfwiki.com/wiki/" + tdollName);
		} else {
			infoPanel.withUrl("https://en.gfwiki.com/wiki/" + tdollName);
		}

		// Change embed bar color
		int rarity = data.getInt("rarity");
		int color = 16760576; // 5* "Fluorescent Orange"
		if (rarity == 6) {
			// Collab EXTRA A lighter tint of Amethyst
			color = 12558303;
		} else if (rarity == 4) {
			// 4* "Android Green"
			color = 10798649;
		} else if (rarity == 3) {
			// 3* A tint of Dark Turquoise
			color = 1965311;
		} else if (rarity == 2) {
			// 2* A tint of Dark Grey
			color = 14671839;
		}
		infoPanel.withColor(color);

		// Description
		String stars = "";
		if (rarity == 6) {
			stars = "EXTRA";
		} else {
			for (int i = 0; i < rarity; i++) {
				stars += "☆";
			}
		}
		infoPanel.withDesc(data.getString("full_name") + "\n" + stars + " " + data.getString("class"));

		// Buff tiles as thumbnail
		infoPanel.withThumbnail(URL_HEADER + data.getString("formation") + ".png");

		// Image of T-doll (default cg)
		if (mod3) {
			infoPanel.withImage(URL_HEADER + cg.getJSONObject("mod3").getString("normal") + ".png");
		} else {
			infoPanel.withImage(URL_HEADER + cg.getJSONObject("default").getString("normal") + ".png");
		}

		// Buff Tiles effect
		infoPanel.appendField("Buff Tiles", data.getString("buff"), false);

		// Skill 1
		JSONObject skillJson;
		if (mod3) {
			skillJson = data.getJSONObject("skill1");
		} else {
			skillJson = data.getJSONObject("skill");
		}
		int cd = skillJson.getInt("cd");
		String cooldown = " (CD: ";
		if (cd == 0) {
			// Passive
			cooldown = " (Passive)";
		} else if (cd < 0) {
			// Cooldown lasts entire battle
			cooldown += skillJson.getInt("init_cd") + "→Entire Duration of Battle";
		} else {
			cooldown += skillJson.getInt("init_cd") + "→" + cd + "s)";
		}
		String skill1Name = "Skill (Lv.10) - " + skillJson.getString("name") + cooldown;
		if (mod3) {
			skill1Name = "Skill 1 (Lv.10) - " + skillJson.getString("name") + cooldown;
		}
		infoPanel.appendField(skill1Name, skillJson.getString("effect"), false);

		// Skill 2 (mod3)
		if (mod3) {
			String skill2Name = "Skill 2 (Lv.10) - " + data.getJSONObject("skill2").getString("name") + " (Passive)";
			infoPanel.appendField(skill2Name, data.getJSONObject("skill2").getString("effect"), false);
		}

		// Equipment Slots
		String equipSlots = "**1.** " + data.getString("slot_1") + "\n";
		equipSlots += "**2.** " + data.getString("slot_2") + "\n";
		equipSlots += "**3.** " + data.getString("slot_3");
		infoPanel.appendField("Equipment Slots", equipSlots, false);

		// T-Doll is craftable + construction timer
		if (!mod3) {
			boolean normalCraftable = data.getBoolean("craftable_normal");
			boolean heavyCraftable = data.getBoolean("craftable_heavy");
			if (normalCraftable || heavyCraftable) {
				String craft = ((normalCraftable) ? "Normal, " : "") + ((heavyCraftable) ? "Heavy" : "");
				infoPanel.appendField("Constructable", craft.replaceAll(", $", ""), true);
				infoPanel.appendField("Construction Timer", data.getString("craft_timer"), true);
			}

			// T-Doll is droppable
			if (data.getBoolean("drop")) {
				infoPanel.appendField("Drops From", data.getString("drop_locations"), false);
			}

			// T-Doll is a Reward
			if (data.getBoolean("reward")) {
				infoPanel.appendField("Reward", data.getString("reward_reason"), false);
			}
		}

		// T-doll's Exclusive Equipment
		try {
			JSONArray equipList = tdollData.getJSONArray("exclusive_equipment");
			String exclusiveEquips = "";
			for (int i = 0; i < equipList.length(); i++) {
				exclusiveEquips += equipList.get(i) + "\n";
			}
			infoPanel.appendField("Exclusive Equipment", exclusiveEquips, false);
		} catch (Exception e) {
			// No exclusive Equipment found
		}

		// T-doll's Skins
		try {
			JSONArray skinList = cg.getJSONObject("skins").getJSONArray("list");
			String skins = "";
			for (int i = 0; i < skinList.length(); i++) {
				skins += skinList.get(i) + "\n";
			}
			infoPanel.appendField("Skins", skins.replace("(Live2D)", "**(Live2D)**"), false);
		} catch (Exception e) {
			// No skins found
		}

		// T-doll's voice actress and illustrator
		infoPanel.appendField("CV", data.getString("cv"), true);
		infoPanel.appendField("Illustrator", data.getString("artist"), true);

		// Display current CG/skin name as footer text
		try {
			infoPanel.withFooterText("Default 1/" + (2 + cg.getJSONObject("skins").getJSONArray("list").length() * 2));
		} catch (Exception e) {
			infoPanel.withFooterText("Default 1/2");
		}

		return infoPanel.build();
	}

	/*
	private EmbedJsonStringBuilder displayTdollInfo(String name) {

		EmbedJsonStringBuilder infoPanel = new EmbedJsonStringBuilder();
		JSONObject data = null;
		try {
			data = getTdollData(name).getJSONObject("");
		} catch (Exception e) {
			return errorEmbedJson("That T-Doll's data was not found in the database");
		}
		int rarity = data.getInt("rarity");

		// Title
		infoPanel.withTitle(data.getString("name"));

		// Link title to gfwiki page
		infoPanel.withUrl("https://en.gfwiki.com/wiki/" + data.getString("name").replace(" ", "_"));

		// Change side bar color
		String color = "16760576";
		if (rarity == 4) {
			// 4* "Android Green"
			color = "10798649";
		} else if (rarity == 3) {
			// 3* A tint of Dark Turquoise
			color = "1965311";
		} else if (rarity == 2) {
			// 2* A tint of Dark Grey
			color = "14671839";
		}
		infoPanel.withColor(color);

		// Description
		String stars = "";
		for (int i = 0; i < rarity; i++) {
			stars += "☆";
		}
		infoPanel.withDesc(stars + " " + data.getString("class"));

		// Buff tiles as thumbnail
		infoPanel.withThumbnail("https://cdn.discordapp.com/attachments/487029209114345502/" + data.getString("formation"));

		// Image of T-doll (default skin)
		infoPanel.withImage("https://cdn.discordapp.com/attachments/487029209114345502/" + data.getString("portrait") + ".png");

		// Extra fields
		infoPanel.appendField("Buff Tiles", data.getString("buff"), false);
		infoPanel.appendField("Skill - " + data.getJSONObject("skill").getString("name"), data.getJSONObject("skill").getString("effect"), false);

		// T-doll's Exclusive Equipment
		try {
			JSONArray equipList = dataJson.getJSONObject(name).getJSONArray("exclusive_equipment");
			String exclusiveEquips = "";
			for (int i = 0; i < equipList.length(); i++) {
				exclusiveEquips += equipList.get(i) + ", ";
			}
			infoPanel.appendField("Exclusive Equipment", exclusiveEquips.replaceAll(", $", ""), false);
		} catch (Exception e) {}

		// T-doll's Skins
		try {
			JSONArray skinList = dataJson.getJSONObject(name).getJSONArray("skins");
			String skins = "";
			for (int i = 0; i < skinList.length(); i++) {
				skins += skinList.get(i) + ", ";
			}
			infoPanel.appendField("Skins", skins.replaceAll(", $", ""), false);
		} catch (Exception e) {}

		// T-doll's voice actress and illustrator
		infoPanel.appendField("CV", data.getString("cv"), true);
		infoPanel.appendField("Illustrator", data.getString("artist"), true);

		// Display "Normal"/"Damaged" CG as footer text
		infoPanel.withFooter("Normal 1/2");


		return infoPanel.build();
	}
	*/

	private void loadJson() {
		tdollDataJson = loadJsonFile("TdollData.json");
		timerDataJson = loadJsonFile("ProductionTimers.json");
		//equipDataJson = loadJsonFile("EquipmentData.json");
		mapDataJson = loadJsonFile("MapData.json");
	}

	private JSONObject loadJsonFile(String fileName) {

		try {
			String dataPath = System.getProperty("user.dir") + "/data/GF/";

			// Load data
			return new JSONObject(new Scanner(new File(dataPath + fileName)).useDelimiter("\\A").next());

		} catch (FileNotFoundException e) {
			Main.displayError("Data does not exist");
		} catch (Exception e) {
			Main.displayError(e.getMessage() + " occurred while attempting to read the data");
			e.printStackTrace();
		}

		return null;
	}

	public String getHelp() {
		if (this.command.equals("tdoll")) {
			return BotHelp.formatHelpMessage("t-doll", "tdoll (mod3)", "Displays CG and detailed information of that T-Doll ('s digimind upgrade)");
		} else if (this.command.equals("timer")) {
			return BotHelp.formatHelpMessage(this.command, "h:mm", "Displays the potential T-Dolls with that construction timer");
		} else if (this.command.equals("map")) {
			return BotHelp.formatHelpMessage(this.command, "(event operation) map", "Displays information about that map with enemies that have fixed starting positions (enemies that have random starting locations or appear later will not be shown).");
		}

		return "";
	}
}
