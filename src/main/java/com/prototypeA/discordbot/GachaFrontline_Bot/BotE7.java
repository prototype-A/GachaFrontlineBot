package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Consumer;


public class BotE7 extends CommandModule {

	private static JSONObject artifactDataJson;
	private final static String URL_HEADER = Main.getParameter("E7AssetHeader");
	private final static String E7_COMMAND = Main.getParameter("E7Command");

	public BotE7() {
		super("Epic Seven", E7_COMMAND);

		// Add available commands to list
		COMMANDS.put("artifact", new BotE7(E7_COMMAND, "artifact"));
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
			}
		}

	}

	private JSONObject getArtifactData(String name) {
		try {
			// Check if short name
			return artifactDataJson.getJSONObject(artifactDataJson.getJSONObject(name)
																	.getString("data"));
		} catch (Exception e) {}

		return artifactDataJson.getJSONObject(name);
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
		String artifactDesc = artifactRarity + " ";

		// Artifact class restriction
		try {
			artifactDesc += artifactJson.getString("class_restriction") +
							" Exclusive";
		} catch (Exception e) {}

		// Embed color
		String embedColor = "16730837";

		// Thumbnail
		String artifactThumbnailUrl = URL_HEADER +
										artifactJson.getString("img_small") +
										".png";

		// Image
		String artifactImgUrl = URL_HEADER + artifactJson.getString("img") +
								".png";

		// Build Embed
		JsonEmbed.EmbedJsonStringBuilder artifactInfo = new JsonEmbed.EmbedJsonStringBuilder();
		artifactInfo.withTitle(artifactName)
					.withUrl(artifactUrl)
					.withDesc(artifactDesc)
					.withColor(embedColor)
					.withThumbnail(artifactThumbnailUrl)
					.withImage(artifactImgUrl);

		// Stats
		String hpStat = "" + artifactJson.getInt("hp_base") + " - " +
						artifactJson.getInt("hp_max");
		String attackStat = "" + artifactJson.getInt("atk_base") + " - " +
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

	private void loadJson() {
		artifactDataJson = loadJsonFile("/data/E7/Artifacts.json");
	}

	public String getHelp() {
		if (this.SUBCOMMAND.equals("artifact")) {
			return BotHelp.formatModuleHelpMessage(this.COMMAND, "artifact", "artifact_name", "Displays detailed information of the specified artifact.");
		}

		return "";
	}
}
