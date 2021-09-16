package com.prototypeA.discordbot.GachaFrontline_Bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class Main {

	private static final String SETTINGS_FILE_NAME = "bot.settings";
	private static final String SETTINGS_FILE_LOCATION = "data/" + SETTINGS_FILE_NAME;
	private static final String SERVERS_DIR = "data/Servers";
	private static HashMap<String, String> settings;
	private static HashMap<String, HashMap<String, String>> serverSettings;
	private static Instance bot;


	/**
	 * Load the base bot settings
	 */
	private static void loadBotSettings() {
		try {
			settings = new HashMap<String, String>();
			Scanner reader = new Scanner(new File(SETTINGS_FILE_LOCATION));

			// Iterate over all lines of text in file
			while (reader.hasNext()) {
				putSetting(reader.nextLine(), settings);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			ConsoleUtils.printError("Error with \"" + SETTINGS_FILE_LOCATION + "\"", e);
		}
	}

	/**
	 * Load the custom settings for the bot's servers
	 */
	private static void loadServerSettings() {
		serverSettings = new HashMap<String, HashMap<String, String>>();

		for (File file : new File(SERVERS_DIR).listFiles()) {
			// Check each server directory
			if (file.isDirectory()) {
				String serverID = file.getName();
				serverSettings.put(serverID, new HashMap<String, String>());

				try {
					// Read server settings
					File settingsFile = new File(SERVERS_DIR + serverID + "/" + SETTINGS_FILE_NAME);
					Scanner reader = new Scanner(settingsFile);

					while (reader.hasNext()) {
						putSetting(reader.nextLine(), serverSettings.get(serverID));
					}
					reader.close();
				} catch (Exception e) {}
			}
		}
	}


	/**
	 * Split lines read from the bot's settings file and split them, storing
	 * it as a pair into a settings hashmap
	 *
	 * @param line The line read from the settings file to split and store
	 */
	private static void putSetting(String line, HashMap<String, String> map) {
		// Ignore empty lines or lines that start with a '#' (comment)
		if (!(line.trim().equals("") || line.charAt(0) == '#')) {
			String[] pair = line.split(":", 2);
			pair[0] = pair[0].trim();
			pair[1] = pair[1].trim();
			map.put(pair[0], (pair[1] == null) ? "" : pair[1]);
		}
	}

	/**
	 * Returns a global bot setting parameter value from "bot.settings"
	 *
	 * @param param The setting to get the value for
	 * @return The value of the setting
	 */
	public static String getParameter(String param) {
		if (param == null) {
			return null;
		}

		try {
			return settings.get(param);
		} catch (Exception e) {}

		return null;
	}

	/**
	 * Returns a custom server setting parameter value
	 *
	 * @param serverID The ID of the server to get the setting value for
	 * @param param The setting to get the value for
	 * @return The value of the setting
	 */
	public static String getServerParameter(String serverID, String param) {
		if (serverID == null || param == null) {
			return null;
		}

		try {
			return serverSettings.get(serverID).get(param);
		} catch (Exception e) {}

		return null;
	}

	/**
	 * Returns the key that is used to issue bot commands
	 * 
	 * @return The string that is used to issue bot commands
	 */
	public static String getKey() {
		return getParameter("CommandTrigger");
	}

	/**
	 * Returns the key for the specified API, if found
	 * 
	 * @param api The name of the API to get the key for
	 * @return The API key, or null if the key was not found
	 */
	public static String getApiKey(String api) {
		return getParameter(api);
	}

	/**
	 * Writes and saves a server parameter setting for a
	 * specified server
	 * 
	 * @param serverID The ID of the server
	 * @param setting The parameter to write
	 * @param value The value of the setting to write
	 */
	public static void updateServerSetting(String serverID, String setting, String value) {
		String filePath = SERVERS_DIR + "/" + serverID + "/" + SETTINGS_FILE_NAME;
		try {
			File settingsFile = new File(filePath);
			Scanner reader = new Scanner(settingsFile);
			FileWriter writer = new FileWriter(settingsFile);
			String serverSettings = "";

			// Read file
			while (reader.hasNext()) {
				String line = reader.nextLine();

				if (line.startsWith(setting + ":")) {
					// Overwrite previous value
					serverSettings += setting + ": " + value;
				} else {
					serverSettings += line;
				}
			}
			reader.close();

			// Update file
			writer.write(serverSettings);
			writer.close();
		} catch (FileNotFoundException e1) {
			// Create server settings file
			try {
				FileWriter writer = new FileWriter(new File(filePath));
				writer.write(setting + ": " + value);
				writer.close();
			} catch (Exception e) {
				ConsoleUtils.printError("Failed to create new settings file for server [" + serverID + "]", e);
			}
		} catch (IOException e2) {
			ConsoleUtils.printError("Failed to write settings for server [" + serverID + "]", e2);
		}
	}


	/**
	 * Main is the class executed when the bot is launched
	 * and handles loggin in and running the instance(s)
	 * of the bot
	 */
	public static void main(String[] args) {

		ConsoleUtils.origin = "Bot";
		ConsoleUtils.printMessage("Launching...");

		try {
			// Get launch args
			boolean testMode = false;
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-test") ||
					args[i].equals("-t") ||
					args[i].equals("--test")) {
					// Testing mode
					testMode = true;
				}
			}

			// Read settings
			loadBotSettings();
			loadServerSettings();

			// Kill the launch if token or key was not specified in settings file
			String botToken = "";
			String botTrigger = "";
			
			if (testMode) {
				// Load test bot token and trigger instead
				ConsoleUtils.printWarning("Launching in TEST mode");
				botToken = settings.get("TestToken");
				botTrigger = settings.get("TestCommandTrigger");
				if (botToken.equals("") || botTrigger.equals("")) {
					throw new Exception("Test Bot token or command trigger not found");
				}
			} else {
				botToken = settings.get("Token");
				botTrigger = settings.get("CommandTrigger");
				if (botToken.equals("") || botTrigger.equals("")) {
					throw new Exception("Bot token or command trigger not found");
				}
			}

			ConsoleUtils.printMessage("Token and command trigger found. Logging in...");
			bot = new Instance(botToken, botTrigger);
		} catch (Exception e) {
			ConsoleUtils.printError("Failed to launch bot", e);
			e.printStackTrace();
		}

	}

}
