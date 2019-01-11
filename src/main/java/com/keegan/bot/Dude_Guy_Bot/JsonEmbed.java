package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class JsonEmbed {

	public static EmbedObject embedMessage(String message, String color) {
		return new EmbedBuilder()
			.withTitle(message)
			.withColor(Integer.parseInt(color))
			.build();
	}

	public static EmbedObject errorEmbed(String message) {
		return embedMessage(message, Main.getParameter("EmbedFailureColor"));
	}

	public static EmbedJsonStringBuilder successEmbedJson(String message) {
		return embedJson(message, Main.getParameter("EmbedSuccessColor"));
	}

	public static EmbedJsonStringBuilder errorEmbedJson(String message) {
		return embedJson(message, Main.getParameter("EmbedFailureColor"));
	}

	public static void embedAsWebhook(String webhookToken, String content) {
		webhookPost(webhookToken, "content=" + content, "application/json; charset=UTF-8");
	}

	public static void embedAsWebhook(String webhookToken, EmbedJsonStringBuilder content) {
		webhookPost(webhookToken, content.toString(), "application/json; charset=UTF-8");
	}

	private static EmbedJsonStringBuilder embedJson(String message, String color) {
		EmbedJsonStringBuilder embed = new EmbedJsonStringBuilder();
		return embed.withTitle(message).withColor(color);
	}

	/**
	 * Based off of Discord4J's EmbedBuilder
	 */
	public static class EmbedJsonStringBuilder {

		private String titleJson;
		private String urlJson;
		private String colorJson;
		private String descJson;
		private String thumbnailJson;
		private String imageJson;
		private String fieldsJson;
		private String fieldJsonOpen = "\"fields\":[";
		private String footerJson;
		private String footerJsonOpen = "\"footer\":{";


		public EmbedJsonStringBuilder() {
			titleJson = "";
			urlJson = "";
			colorJson = "";
			descJson = "";
			thumbnailJson = "";
			imageJson = "";
			fieldsJson = "";
			footerJson = "}";
		}

		public EmbedJsonStringBuilder withTitle(String title) {
			titleJson = "\"title\":\"" + title + "\",";
			return this;
		}

		public EmbedJsonStringBuilder withUrl(String url) {
			urlJson = "\"url\":\"" + url + "\",";
			return this;
		}

		public EmbedJsonStringBuilder withColor(String color) {
			colorJson = "\"color\":" + color + ",";
			return this;
		}

		public EmbedJsonStringBuilder withDesc(String desc) {
			descJson = "\"description\":\"" + desc + "\",";
			return this;
		}

		public EmbedJsonStringBuilder withThumbnail(String url) {
			thumbnailJson = "\"thumbnail\":{\"url\":\"" + url + ".png\"},";
			return this;
		}

		public EmbedJsonStringBuilder withImage(String url) {
			imageJson = "\"image\":{\"url\":\"" + url + "\"},";
			return this;
		}

		public EmbedJsonStringBuilder withFooter(String text) {
			footerJson = "\"text\":\"" + text + "\"},";
			return this;
		}

		public EmbedJsonStringBuilder withFooter(String text, String iconUrl) {
			footerJson = "\"text\":\"" + text + "\",\"icon_url\":\"" + iconUrl + "\"},";
			return this;
		}

		public EmbedJsonStringBuilder appendField(String title, String content, boolean inline) {
			fieldsJson += "{\"name\":\"" + title + "\",\"value\":\"" + 
							content + "\",\"inline\":" + 
							String.valueOf(inline) + "},";
			return this;
		}

		public String build() {
			return this.toString();
		}

		public String toString() {
			String jsonString = "{\"embeds\":[{" + titleJson + urlJson + 
								colorJson + descJson + thumbnailJson + 
								imageJson + fieldJsonOpen;
			if (!fieldsJson.equals("")) {
				jsonString += fieldsJson.substring(0, fieldsJson.length()-1);
			}
			jsonString += "]," + footerJsonOpen + footerJson + "}]}";
			jsonString = jsonString.replace("\"fields\":[],", "").replace("\"footer\":{}", "");

			int lastCommaIndex = jsonString.lastIndexOf(',');
			jsonString = jsonString.substring(0, lastCommaIndex) + 
						jsonString.substring(lastCommaIndex + 1, 
											jsonString.length());

			return jsonString;
		}
	}

	/**
	 * Send a HTTP POST request to the webhook API url to send a message as that webhook
	 *
	 * @param webhook The webhook to send the message as
	 * @param content The string to send as the message
	 */
	public static void webhookPost(String webhookToken, String content, String contentType) {

		try {
			URL webhookUrl = new URL("https://discordapp.com/api/webhooks/" + webhookToken);
			HttpsURLConnection webhookConnection = (HttpsURLConnection)webhookUrl.openConnection();
			webhookConnection.setRequestMethod("POST");
			webhookConnection.setRequestProperty("Authorization", Main.getParameter("Token"));
			webhookConnection.setRequestProperty("User-Agent", "DiscordBot");
			webhookConnection.setRequestProperty("Content-Type", contentType);
			webhookConnection.setDoOutput(true);
			DataOutputStream outStream = new DataOutputStream(webhookConnection.getOutputStream());

			//Main.displayMessage("Sending\u001B[1m\033[1;35m POST\033[1;0m\u001B[0m request to webhook: " + webhookUrl);

			// Send message
			outStream.write(content.getBytes("UTF-8"));
			outStream.close();

			int responseCode = webhookConnection.getResponseCode();
			//Main.displayMessage("Response Code: " + responseCode);
			if (responseCode == 401 || responseCode == 403) {
				Main.displayMessage("POST parameters: " + content);
				throw new DiscordException(Integer.toString(responseCode));
			}

			BufferedReader inStreamReader = new BufferedReader(new InputStreamReader(webhookConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = inStreamReader.readLine()) != null) {
				response.append(inputLine);
			}
			inStreamReader.close();
			//Main.displayMessage("Response Message: " + response);
		} catch (DiscordException e) {
			Main.displayError("Returned Error Code " + e.getMessage() + " while posting as webhook");
			//sendTempMessage("An authentication error occurred");
		} catch (Exception e) {
			Main.displayError(e.getMessage() + " while executing webhook", e);
		}
	}

}
