package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Consumer;


public class BotE7 extends CommandModule {

	private static JSONObject artifactDataJson;
	private static JSONObject catalystDataJson;
	private final static String URL_HEADER = Main.getParameter("E7AssetHeader");
	private final static String E7_COMMAND = Main.getParameter("E7Command");

	public BotE7() {
		super("Epic Seven", E7_COMMAND);

		// Add available commands to list
		COMMANDS.put("artifact", new BotE7(E7_COMMAND, "artifact"));
		COMMANDS.put("catalyst", new BotE7(E7_COMMAND, "catalyst"));
	}

	public BotE7(String command, String subcommand) {
		super("Epic Seven", E7_COMMAND, command, subcommand);

		// Load all associated JSON
		loadJson();
	}

	public void run() {
		
		// Check for permissions
		if (canIssueBotCommands()) {
			String arg = getAsOneArg(true).replaceAll("\\s", "").toLowerCase();

			if (this.SUBCOMMAND.equals("artifact")) {
				// Display artifact info
				JsonEmbed.embedAsWebhook(Main.getParameter("E7ArtifactWebhook"),
											embedArtifactInfo(arg));
			} else if (this.SUBCOMMAND.equals("catalyst")) {
				// Display catalyst info
				JsonEmbed.embedAsWebhook(Main.getParameter("E7CatalystWebhook"),
											embedCatalystInfo(arg));
			}
		}

	}

	/**
	 * Retrieves the data of the specified artifact
	 *
	 * @param name The name of the artifact to retrieve
	 * @return The JSONObject containing the specified artifact's data
	 */
	private JSONObject getArtifactData(String name) {
		try {
			// Check if short name
			return artifactDataJson.getJSONObject(artifactDataJson.getJSONObject(name)
																	.getString("data"));
		} catch (Exception e) {}

		return artifactDataJson.getJSONObject(name);
	}

	/**
	 * Retrieves the data of the specified catalyst
	 *
	 * @param name The name of the catalyst to retrieve
	 * @return The JSONObject containing the specified catalyst's data
	 */
	private JSONObject getCatalystData(String name) {
		try {
			// Check if short name
			return catalystDataJson.getJSONObject(catalystDataJson.getJSONObject(name)
																	.getString("data"));
		} catch (Exception e) {}

		return catalystDataJson.getJSONObject(name);
	}

	
	private JsonEmbed.EmbedJsonStringBuilder embedArtifactInfo(String name) {

		JSONObject artifactJson = getArtifactData(name);

		// Artifact name
		String artifactName = artifactJson.getString("name");

		// Link to E7x
		String artifactUrl = "https://epic7x.com/artifact/" +
								artifactName.replace(" ", "-")
											.replace("&", "and")
											.toLowerCase();

		// Artifact * rarity
		int rarity = artifactJson.getInt("rarity");
		String artifactRarity = "";
		for (int i = 0; i < rarity; i++) {
			artifactRarity += "🟊";
		}
		String artifactDesc = artifactRarity + "\\n";

		// Artifact class restriction
		try {
			artifactDesc += artifactJson.getString("class_restriction") +
							" Exclusive";
		} catch (Exception e) {}

		// Limited artifact
		try {
			// Only boolean value is false, which means not limited
			artifactJson.getBoolean("limited");
		} catch (Exception e) {
			artifactDesc += "\\n" + artifactJson.getString("limited");
		}

		// Thumbnail
		String artifactThumbnailUrl = URL_HEADER +
										artifactJson.getString("img_small") +
										".png";

		// Image
		String artifactImgUrl = URL_HEADER + artifactJson.getString("img") +
								".png";

		// Lore
		String artifactLore = artifactJson.getString("desc")
											.replace("\"", "\\\"");

		// Build Embed
		JsonEmbed.EmbedJsonStringBuilder artifactInfo = new JsonEmbed.EmbedJsonStringBuilder();
		artifactInfo.withTitle(artifactName)
					.withUrl(artifactUrl)
					.withDesc(artifactDesc)
					.withColor("16730837")
					.withThumbnail(artifactThumbnailUrl)
					.withImage(artifactImgUrl)
					.withFooter(artifactLore);

		// Stats
		String hpStat = "" + artifactJson.getInt("hp_base") + " ~ " +
						artifactJson.getInt("hp_max");
		String attackStat = "" + artifactJson.getInt("atk_base") + " ~ " +
							artifactJson.getInt("atk_max");
		artifactInfo.appendField("HP", hpStat, true);
		artifactInfo.appendField("Attack", attackStat, true);

		// Skill
		String baseSkill = artifactJson.getString("skill_min");
		String maxSkill = artifactJson.getString("skill_max");
		if (!baseSkill.equals(maxSkill)) {
			artifactInfo.appendField("Skill (Lv.1)", baseSkill, false);
			artifactInfo.appendField("Skill (Lv.11)", maxSkill, false);
		} else {
			// Skill does not change when enhanced
			artifactInfo.appendField("Skill", baseSkill, false);
		}


		return artifactInfo;
	}


	private JsonEmbed.EmbedJsonStringBuilder embedCatalystInfo(String name) {

		JSONObject catalystJson = getCatalystData(name);

		// Catalyst name
		String catalystName = catalystJson.getString("name");

		// Link to E7x
		String catalystUrl = "https://epic7x.com/material/" +
								catalystName.replace(" ", "-")
											.toLowerCase();

		// Rarity
		String rarity = catalystJson.getString("rarity");

		// Embed color
		String embedColor = (rarity.equals("Epic")) ? "11216674" : "4680066";

		// Thumbnail
		String catalystThumbnailUrl = URL_HEADER +
										catalystJson.getString("img") +
										".jpg";

		// Embed body
		String catalystBody = rarity +
								" Catalyst\\n" +
								"Used to Awaken and Skill Enhance " +
								catalystJson.getString("horoscope") +
								" Characters";

		// Description footer
		String catalystDesc = catalystJson.getString("desc");

		// Build Embed
		JsonEmbed.EmbedJsonStringBuilder catalystInfo = new JsonEmbed.EmbedJsonStringBuilder();
		catalystInfo.withTitle(catalystName)
					.withUrl(catalystUrl)
					.withDesc(catalystBody)
					.withColor(embedColor)
					.withThumbnail(catalystThumbnailUrl)
					.withFooter(catalystDesc);

		// AP Shop locations
		String apShopTitle = "AP Shops";
		String catalystApShopTitle = (rarity.equals("Epic")) ?
										apShopTitle + " (2x 400 AP)":
										apShopTitle + " (5x 120 AP)";
		catalystInfo.appendField(catalystApShopTitle,
									catalystJson.getString("ap_shops")
												.replace(", ", "\\n"),
									true);

		// Episode 1 drop locations
		String ep1DropTitle = "Episode 1";
		String catalystEp1DropTitle = (rarity.equals("Epic")) ?
										ep1DropTitle + " UH" :
										ep1DropTitle + " (+ UH)";
		catalystInfo.appendField(catalystEp1DropTitle,
									catalystJson.getString("ep1_drops")
												.replace(", ", "\\n"),
									true);

		// Episode 2 drop locations
		try {
			catalystInfo.appendField("Episode 2",
										catalystJson.getString("ep1_drops")
													.replace(", ", "\\n"),
										true);
		} catch (Exception e) {}

		return catalystInfo;
	}

	/**
	 * Loads the .json files required by this module
	 */
	private void loadJson() {
		artifactDataJson = loadJsonFile("/data/E7/Artifacts.json");
		catalystDataJson = loadJsonFile("/data/E7/Catalysts.json");
	}

	/**
	 * Returns help messages for this module's commands
	 */
	public String getHelp() {
		if (this.SUBCOMMAND.equals("artifact")) {
			return BotHelp.formatModuleHelpMessage(this.COMMAND, this.SUBCOMMAND, "artifact_name", "Displays detailed information of the specified artifact.");
		} else if (this.SUBCOMMAND.equals("catalyst")) {
			return BotHelp.formatModuleHelpMessage(this.COMMAND, this.SUBCOMMAND, "catalyst_name", "Displays detailed information of the specified catalyst.");
		}

		return "";
	}
}
