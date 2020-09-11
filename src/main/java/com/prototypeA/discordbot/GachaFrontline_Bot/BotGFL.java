package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.rest.util.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Consumer;


public class BotGFL extends CommandModule {

	private static JSONObject tdollDataJson;
	private static JSONObject fairyDataJson;
	private static JSONObject tdollTimerDataJson;
	private static JSONObject equipTimerDataJson;
	//private JSONObject equipDataJson;
	private static JSONObject mapDataJson;
	private final static String URL_HEADER = Main.getParameter("AssetHeader");
	private final static String GFL_COMMAND = Main.getParameter("GFLCommand");


	public BotGFL() {
		super("Girls Frontline", GFL_COMMAND);

		// Add available commands to list
		COMMANDS.put("craft", new BotGFL(GFL_COMMAND, "constructiontimer"));
		//COMMANDS.put("equip", new BotGFL(GFL_COMMAND, "equip"));
		//COMMANDS.put("equipment", new BotGFL(GFL_COMMAND, "equip"));
		COMMANDS.put("fairy", new BotGFL(GFL_COMMAND, "fairy"));
		COMMANDS.put("map", new BotGFL(GFL_COMMAND, "map"));
		COMMANDS.put("mix", new BotGFL(GFL_COMMAND, "mix"));
		COMMANDS.put("prod", new BotGFL(GFL_COMMAND, "productiontimer"));
		COMMANDS.put("production", new BotGFL(GFL_COMMAND, "productiontimer"));
		COMMANDS.put("tdoll", new BotGFL(GFL_COMMAND, "tdoll"));
		COMMANDS.put("t-doll", new BotGFL(GFL_COMMAND, "tdoll"));
		COMMANDS.put("timer", new BotGFL(GFL_COMMAND, "timer"));
	}

	public BotGFL(String command, String subcommand) {
		super("Girls Frontline", GFL_COMMAND, command, subcommand);

		// Load all associated JSON
		loadJson();
	}

	public void run() {
		
		// Check for permissions
		if (canIssueBotCommands()) {

			// Format inputted T-Doll name
			String arg = "";
			try {
				arg = getAsOneArg(true).replaceFirst("^(Am )", "")
										.replaceFirst("^(FF )", "")
										.replaceFirst("^(Gd )", "")
										.replaceFirst("^(Gr )", "")
										.replaceAll("\\s", "")
										.replace(".", "")
										.toLowerCase();
			} catch (Exception e) {}

			// Display T-Doll info
			if (this.SUBCOMMAND.equals("tdoll")) {
				// Whether to display mod3 info or not
				arg = arg.replace("-", "");
				boolean mod3 = false;
				if (arg.length() >= 5) {
					if (arg.substring(arg.length() - 4, arg.length()).toLowerCase().equals("mod3")) {
						mod3 = true;
						arg = arg.substring(0, arg.length() - 4);
					}
				}

				// Start emoji reaction navigation thread for a period of time
				Message msg = sendMessage(displayTdollInfo(arg, mod3));
				try {
					JSONObject cgJson = getTdollData(arg).getJSONObject("cg");
					CGScroll cgScroller = new TDollInfoEmbed(gateway, msg,
																cmdMessage.getAuthor().get(),
																cgJson, mod3);
					cgScroller.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (this.SUBCOMMAND.equals("fairy")) {
				// Display fairy info
				// Start emoji reaction navigation thread for a period of time
				Message msg = sendMessage(displayFairyInfo(arg));
				try {
					JSONObject cgJson = getFairyData(arg).getJSONObject("cg");
					CGScroll cgScroller = new FairyInfoEmbed(gateway, msg,
																cmdMessage.getAuthor().get(),
																cgJson);
					cgScroller.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (this.SUBCOMMAND.equals("equip")) {
				//sendMessage(displayEquipmentInfo(arg));
			} else if (this.SUBCOMMAND.equals("timer")) {
				// Display both potential T-Doll and equipment from a given timer
				JsonEmbed.embedAsWebhook(Main.getParameter("TimerWebhook"), embedEquipmentTdollsFromTimer(arg));
			} else if (this.SUBCOMMAND.equals("constructiontimer")) {
				// Display potential T-Doll from given construction timer
				JsonEmbed.embedAsWebhook(Main.getParameter("TimerWebhook"), embedTdollsFromTimer(arg));
	 		} else if (this.SUBCOMMAND.equals("productiontimer")) {
				// Display potential equipment from given production timer
				JsonEmbed.embedAsWebhook(Main.getParameter("TimerWebhook"), embedEquipmentFromTimer(arg));
			} else if (this.SUBCOMMAND.equals("map")) {
				// Display specified map info
				JsonEmbed.embedAsWebhook(Main.getParameter("MapWebhook"), displayMapInfo(arg));
			} else if (this.SUBCOMMAND.equals("mix")) {
				// Display VA11 Ha11-A drink combinations
				JsonEmbed.embedAsWebhook(Main.getParameter("BartenderWebhook"), displayBartendingInfo());
			}
		}
	}

	private String formatTimer(String timer) {
		// Parse the timer given
		int colonCount = 0;
		for (int i = 0; i < timer.length(); i++) {
			if (timer.charAt(i) == ':') {
				colonCount++;
			}
		}

		try {
			if (colonCount == 0) {
				// e.g. "215"
				timer = timer.charAt(0) + ":" + timer.substring(1) + ":00";
			} else if (colonCount == 1) {
				// e.g. "2:15"
				timer += ":00";
			} else if (colonCount > 2) {
				throw new Exception();
			}
		} catch (Exception e) {
			return null;
		}

		return timer;
	}

	private JsonEmbed.EmbedJsonStringBuilder embedEquipmentTdollsFromTimer(String timer) {

		timer = formatTimer(timer);

		if (timer == null) {
			return JsonEmbed.errorEmbedJson("Invalid timer");
		}

		String tdolls = getTdollsFromTimer(timer);
		String equips = getEquipsFromTimer(timer);

		// No T-Dolls or Equipment with that timer
		if (tdolls == null && equips == null) {
			return JsonEmbed.errorEmbedJson("**There are no T-Dolls or equipment with that timer**");
		}
		if (tdolls == null) {
			tdolls = "None";
		} else if (equips == null) {
			equips = "None";
		}
		return new JsonEmbed.EmbedJsonStringBuilder()
			.withTitle("T-Dolls:")
			.withDesc(tdolls.replace("**Possible T-Dolls:** ", ""))
			.withColor(Main.getParameter("EmbedSuccessColor"))
			.appendField("Equipment:", equips.replace("**Possible Equipment:** ", ""), false);
	}

	private JsonEmbed.EmbedJsonStringBuilder embedTdollsFromTimer(String timer) {

		timer = formatTimer(timer);

		if (timer == null) {
			return JsonEmbed.errorEmbedJson("Invalid timer");
		}

		String tdolls = getTdollsFromTimer(timer);
		if (tdolls == null) {
			return JsonEmbed.errorEmbedJson("**There are no T-Dolls with that construction time**");
		}
		return JsonEmbed.successEmbedJson(tdolls);
	}

	private JsonEmbed.EmbedJsonStringBuilder embedEquipmentFromTimer(String timer) {
		timer = formatTimer(timer);

		if (timer == null) {
			return JsonEmbed.errorEmbedJson("Invalid timer");
		}

		String equips = getEquipsFromTimer(timer);
		if (equips == null) {
			return JsonEmbed.errorEmbedJson("**There are no equipment with that production time**");
		}
		return JsonEmbed.successEmbedJson(equips);
	}

	private String getTdollsFromTimer(String timer) {
		// Get possible T-dolls
		try {
			return "**Possible T-Dolls:** " + tdollTimerDataJson.getString(timer);
		} catch (Exception e) {}

		// No T-dolls with timer
		return null;
	}

	private String getEquipsFromTimer(String timer) {
		// Get possible equipment
		try {
			return "**Possible Equipment:** " + equipTimerDataJson.getString(timer);
		} catch (Exception e) {}

		// No equipment with timer
		return null;
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
			sendTempMessage("Failed to embed map info");
			e.printStackTrace();
		}

		// Map not found
		return JsonEmbed.errorEmbedJson("Map not found");
	}

	private JSONObject getFairyData(String name) {
		// Check if nickname
		try {
			return fairyDataJson.getJSONObject(fairyDataJson.getJSONObject(name).getString("data"));
		} catch (Exception e) {}

		return fairyDataJson.getJSONObject(name);
	}

	private JSONObject getTdollData(String name) {
		// Check if nickname
		try {
			return tdollDataJson.getJSONObject(tdollDataJson.getJSONObject(name).getString("data"));
		} catch (Exception e) {}

		return tdollDataJson.getJSONObject(name);
	}

	private Consumer<? super EmbedCreateSpec> displayTdollInfo(String name,
																boolean mod3) {

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

		// Link title to GFWiki page
		String tdollUrlName = tdollName.replace(" ", "_");
		String tdollUrl = null;
		if (mod3) {
			tdollUrlName = tdollUrlName.substring(0, tdollUrlName.lastIndexOf("_")) + "#Mod3";
			tdollUrl = "https://en.gfwiki.com/wiki/" + tdollUrlName;
		} else {
			tdollUrl = "https://en.gfwiki.com/wiki/" + tdollUrlName;
		}

		// Change embed bar color
		int rarity = data.getInt("rarity");
		int embedColor = 16760576; // 5* "Fluorescent Orange"
		if (rarity == 6) {
			// 6* (5* Digimind Upgrade)
			embedColor = 12339252;
		} else if (rarity == 4) {
			// 4* - "Android Green"
			embedColor = 10798649;
		} else if (rarity == 3) {
			// 3* - A tint of Dark Turquoise
			embedColor = 1965311;
		} else if (rarity == 2) {
			// 2* - A tint of Dark Grey
			embedColor = 14671839;
		} else if (rarity == 0) {
			// Collab EXTRA - A lighter tint of Amethyst
			embedColor = 12558303;
		}

		// Description
		String stars = "";
		if (rarity == 6) {
			stars = "EXTRA";
		} else {
			for (int i = 0; i < rarity; i++) {
				stars += "☆";
			}
		}
		String tdollDesc = data.getString("full_name") + "\n" + stars + " " +
											data.getString("class");

		// Buff tiles as thumbnail
		String thumbnailUrl = URL_HEADER + data.getString("formation") + ".png";

		// Buff Tiles effect
		String tileEffect = data.getString("buff");

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
		String skill1Effect = skillJson.getString("effect");

		// Equipment Slots
		String equipSlots = "**1.** " + data.getString("slot_1") + "\n";
		equipSlots += "**2.** " + data.getString("slot_2") + "\n";
		equipSlots += "**3.** " + data.getString("slot_3");


		// Build embed
		EmbedCreateSpec newEmbed = new EmbedCreateSpec();
		newEmbed = newEmbed.setTitle(tdollName)
							.setUrl(tdollUrl)
							.setColor(Color.of(embedColor))
							.setDescription(tdollDesc)
							.setThumbnail(thumbnailUrl)
							.addField("Buff Tiles", tileEffect, false)
							.addField(skill1Name, skill1Effect, false);

		// Skill 2 (mod3)
		if (mod3) {
			String skill2Name = "Skill 2 (Lv.10) - " + data.getJSONObject("skill2").getString("name") + " (Passive)";
			newEmbed = newEmbed.addField(skill2Name,
								data.getJSONObject("skill2").getString("effect"),
								false);
		}

		// Equipment Slots
		newEmbed = newEmbed.addField("Equipment Slots", equipSlots, false);

		// T-Doll is craftable + construction timer
		if (!mod3) {
			boolean normalCraftable = data.getBoolean("craftable_normal");
			boolean heavyCraftable = data.getBoolean("craftable_heavy");
			if (normalCraftable || heavyCraftable) {
				String craft = ((normalCraftable) ? "Normal, " : "") + ((heavyCraftable) ? "Heavy" : "");
				newEmbed = newEmbed.addField("Constructable",
												craft.replaceAll(", $", ""),
												true);
				newEmbed = newEmbed.addField("Construction Timer",
												data.getString("craft_timer"),
												true);
			}

			// T-Doll is droppable
			if (data.getBoolean("drop")) {
				newEmbed = newEmbed.addField("Drops From",
												data.getString("drop_locations"),
												false);
			}

			// T-Doll is a Reward
			if (data.getBoolean("reward")) {
				newEmbed = newEmbed.addField("Reward",
												data.getString("reward_reason"),
												false);
			}
		}

		// T-doll's Exclusive Equipment
		try {
			JSONArray equipList = tdollData.getJSONArray("exclusive_equipment");
			String exclusiveEquips = "";
			for (int i = 0; i < equipList.length(); i++) {
				exclusiveEquips += equipList.get(i) + "\n";
			}
			newEmbed = newEmbed.addField("Exclusive Equipment", exclusiveEquips, false);
		} catch (Exception e) { /* No exclusive Equipment found */ }

		// T-doll's Skins
		try {
			JSONArray skinList = cg.getJSONObject("skins").getJSONArray("list");
			String skins = "";
			for (int i = 0; i < skinList.length(); i++) {
				skins += skinList.get(i) + "\n";
			}
			newEmbed = newEmbed.addField("Skins",
								skins.replace("(Live2D)", "**(Live2D)**")
										.replace("(Simple Live2D)", "**(Simple Live2D)**"),
								false);
		} catch (Exception e) { /* No skins found */ }

		// T-doll's voice actress and illustrator
		newEmbed = newEmbed.addField("CV", data.getString("cv"), true);
		newEmbed = newEmbed.addField("Illustrator", data.getString("artist"),
										true);

		// Image of T-doll (default cg)
		if (mod3) {
			newEmbed = newEmbed.setImage(URL_HEADER +
								cg.getJSONObject("mod3").getString("normal") +
								".png");
		} else {
			newEmbed = newEmbed.setImage(URL_HEADER +
								cg.getJSONObject("default").getString("normal") +
								".png");
		}

		// Display current CG/skin name as footer text
		try {
			newEmbed = newEmbed.setFooter("Default 1/" + (cg.getJSONObject("default").names().length() + cg.getJSONObject("skins").getJSONArray("list").length() * 2), null);
		} catch (Exception e) {
			newEmbed = newEmbed.setFooter("Default 1/2", null);
		}


		// Build actual embed
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

	private Consumer<? super EmbedCreateSpec> displayFairyInfo(String name) {

		JSONObject data = null;
		JSONObject fairyData = null;
		JSONObject fairyStats = null;
		JSONObject fairySkill = null;
		JSONObject fairyCg = null;

		// Get JSON data
		try {
			data = getFairyData(name);
			fairyData = data.getJSONObject("data");
			fairyStats = data.getJSONObject("stats");
			fairySkill = data.getJSONObject("skill");
			fairyCg = data.getJSONObject("cg");
		} catch (Exception e) {
			return JsonEmbed.errorEmbed("That Technical Fairy's data was not found in the database");
		}

		// Title (Fairy Name)
		String fairyName = fairyData.getString("name");

		// Link title to GFWiki page
		String linkName = fairyName;
		try {
			// Article posted as alternate name in gfwiki
			linkName = fairyData.getString("wiki_name");
		} catch (Exception e) {}
		linkName = linkName.replace(" ", "_").replace("&", "%26");
		String fairyUrl = "https://en.gfwiki.com/wiki/" + linkName;

		// Description (Battle/Strategy-Type)
		String fairyType = fairyData.getString("type");
		String fairyDesc = fairyData.getString("alt_name") + "\n" + fairyType +
												"-Type" +
												((fairyData.getBoolean("extra") ?
													" EXTRA ":
													" ") +  "Technical Fairy");

		// Thumbnail (Apprentice chibi as top right square icon)
		String thumbnailUrl = (URL_HEADER + fairyCg.getString("11") + ".png");

		// Change embed bar color
		int fairyEmbedColor = 16729344;	// Battle ("Orange Red")
		if (fairyType.equals("Strategy")) {
			// Strategy ("Corn Flower Blue")
			fairyEmbedColor = 6591981;
		}

		// Stats
		boolean left = true;
		String fairyStatInfo = "";
		String[] stat = { "dmg", "crit_dmg", "acc", "eva", "armor" };
		String[] statName = { "Damage:\t\t", "Crit. Dmg:\t", "Accuracy:\t",
								"Evasion:\t", "Armor:\t\t" };
		for (int i = 0; i < 5; i++) {
			double statMin = fairyStats.getDouble(stat[i]);
			double statMax = fairyStats.getDouble(stat[i] + "_max");
			if (left) {
				// Left column
				fairyStatInfo += "**" + statName[i] + "**" + statMin + "% → " + statMax + "%\t";
			} else {
				// Right column
				fairyStatInfo += "**" + statName[i] + "**" + statMin + "% → " + statMax + "%\n";
			}
			left = !left;
		}

		// Skill
		String skillName = "Skill (Lv.10) - " + fairySkill.getString("name");
		String skillEffect = fairySkill.getString("effect");

		// Skill cost
		String skillCost = "" + fairySkill.getInt("cost");

		// SKill Cooldown
		int skillCd = fairySkill.getInt("cd");
		String fairySkillCooldown = "None";
		if (skillCd > 0) {
			fairySkillCooldown = skillCd + " Turns";
		}

		// Has Live2D
		String hasL2D = (fairyCg.getBoolean("live2d")) ? "Yes" : "No";


		// Default fairy image
		String defaultImgUrl = URL_HEADER + fairyCg.getString("1") + ".png";

		// Display star level as footer text
		String footerText = "* 1/3";


		// Build embed
		EmbedCreateSpec newEmbed = new EmbedCreateSpec();
		newEmbed = newEmbed.setTitle(fairyName)
							.setUrl(fairyUrl)
							.setDescription(fairyDesc)
							.setThumbnail(thumbnailUrl)
							.setColor(Color.of(fairyEmbedColor))
							.addField("Stats", fairyStatInfo, false)
							.addField(skillName, skillEffect, false)
							.addField("Skill Cost", skillCost, true)
							.addField("Skill Cooldown", fairySkillCooldown, true)
							.addField("Live2D", hasL2D, false)
							.setImage(defaultImgUrl)
							.setFooter(footerText, null);

		// Production time
		if (fairyData.getBoolean("craftable")) {
			newEmbed = newEmbed.addField("Production Time",
											fairyData.getString("production_time"),
											false);
		}

		// Reward reason
		if (fairyData.getBoolean("reward")) {
			newEmbed = newEmbed.addField("Reward From",
											fairyData.getString("reward_reason"),
											false);
		}

	
		// Build actual embed
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

	private JsonEmbed.EmbedJsonStringBuilder displayBartendingInfo() {

		String[] subTitles = { "Cyberpunk Bartending Action",
								"Waifu Bartending Action",
								"Time to mix drinks and change lives",
								"What will you have?" };
		JsonEmbed.EmbedJsonStringBuilder mixingInfo = new JsonEmbed.EmbedJsonStringBuilder();
		Random rngesus = new Random();

		// Title
		mixingInfo.withTitle("**╔∷∷∷∷∷∷∷∷∷♪♪   Menu   ♪♪∷∷∷∷∷∷∷∷∷╗**");

		// Random sub-title
		mixingInfo.withDesc(subTitles[rngesus.nextInt(subTitles.length)]);

		// Embed bar color
		mixingInfo.withColor(Main.getParameter("MixColor"));

		// Jill portrait
		mixingInfo.withThumbnail(Main.getParameter("AssetHeader") + "609215082462838785/Jill_S.png");

		// Big Beer
		mixingInfo.appendField("[Big Beer]", "Flanergide + Bronson Ext + Karmotrine 3");

		// Brandtini
		mixingInfo.appendField("[Brandtini]", "Adelhyde 1 + Adelhyde 2 + Pwd Delta");

		// Piano Woman
		mixingInfo.appendField("[Piano Woman]", "Adelhyde 1 + Bronson Ext + Karmotrine 3");

		// Moonblast
		mixingInfo.appendField("[Moonblast]", "Adelhyde 1 + Adelhyde 2 + Karmotrine 3");

		// Bleeding Jane
		mixingInfo.appendField("[Bleeding Jane]", "Flanergide + Bronson Ext + Pwd Delta");

		// Fringe Weaver
		mixingInfo.appendField("[Fringe Weaver]", "Karmotrine 1 + Karmotrine 2 + Karmotrine 3");

		// Sugar Rush
		mixingInfo.appendField("[Sugar Rush]", "None of the above (Default)");

		// Footer
		mixingInfo.appendField("**╚∷∷∷∷∷∷∷∷∷♪♪  Night Night  ♪♪∷∷∷∷∷∷∷∷∷╝**", "B.T.C.");


		return mixingInfo;
	}

	private void loadJson() {
		tdollDataJson = loadJsonFile("TdollData.json");
		fairyDataJson = loadJsonFile("FairyData.json");
		tdollTimerDataJson = loadJsonFile("TdollTimers.json");
		equipTimerDataJson = loadJsonFile("EquipTimers.json");
		//equipDataJson = loadJsonFile("EquipmentData.json");
		mapDataJson = loadJsonFile("MapData.json");
	}

	private JSONObject loadJsonFile(String fileName) {

		try {
			String dataPath = System.getProperty("user.dir") + "/data/GFL/";

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
		if (this.SUBCOMMAND.equals("tdoll")) {
			return BotHelp.formatModuleHelpMessage(this.COMMAND, "t-doll/tdoll", "tdollname (mod3)", "Displays CG and detailed information of that T-Doll ('s digimind upgrade).");
		} else if (this.SUBCOMMAND.equals("fairy")) {
			return BotHelp.formatModuleHelpMessage(this.COMMAND, this.SUBCOMMAND, "fairyname", "Displays CG and detailed information of that Technical Fairy.");
		} else if (this.SUBCOMMAND.equals("timer")) {
			return BotHelp.formatModuleHelpMessage(this.COMMAND, this.SUBCOMMAND, "h:mm", "Displays the potential T-Dolls and Equipment/Fairies with that construction/production timer.");
		} else if (this.SUBCOMMAND.equals("constructiontimer")) {
			return BotHelp.formatModuleHelpMessage(this.COMMAND, "craft", "h:mm", "Displays the potential T-Dolls with that construction timer.");
		} else if (this.SUBCOMMAND.equals("productiontimer")) {
			return BotHelp.formatModuleHelpMessage(this.COMMAND, "prod/production", "h:mm", "Displays the potential Equipment/Fairies with that production timer.");
		} else if (this.SUBCOMMAND.equals("map")) {
			return BotHelp.formatModuleHelpMessage(this.COMMAND, this.SUBCOMMAND, "(event operation) map", "Displays information about that map with enemies that have fixed starting positions (enemies that have random starting locations or appear later will not be shown).");
		} else if (this.SUBCOMMAND.equals("mix")) {
			return BotHelp.formatModuleHelpMessage(this.COMMAND, this.SUBCOMMAND, "Displays the menu of available drinks and their \"ingredients\" (exclusive equipment) that Jill can mix.");
		}

		return "";
	}
}
