package com.keegan.bot.Dude_Guy_Bot;

import org.json.JSONObject;
import org.json.JSONTokener;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class BotJson {

	/**
	 * Returns the JSON as a JSONObject from jsonUrl
	 *
	 * @param jsonUrl The url of the JSON
	 * @return The JSON as a JSONObject
	 */
	public static JSONObject getJson(String jsonUrl) {

		try {
			URL url = new URL(jsonUrl);
			try {
				return new JSONObject(new JSONTokener(url.openStream()));
			} catch (IOException e) {
				Main.displayError("IOException occurred when obtaining JSON from URL: " + jsonUrl);
			}
		} catch (MalformedURLException e) {
			Main.displayError("Bad url: " + jsonUrl);
		}

		return null;
	}

}
