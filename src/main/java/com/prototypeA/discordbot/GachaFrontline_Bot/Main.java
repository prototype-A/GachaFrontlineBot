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
	private static HashMap<String, String> settings;
	private static HashMap<String, String> serverSettings;
	private static Instance bot;


	/**
	 * Load settings from 'data/bot.settings' into a table
	 */
	private static void loadBotSettings() {
		try {
			settings = new HashMap<String, String>();
			Scanner reader = new Scanner(new File("data/" + SETTINGS_FILE_NAME));

			// Iterate over all lines of text in file
			while (reader.hasNext()) {
				String setting = reader.nextLine();

				// Ignore empty lines or lines that start with a '#' (comment)
				if (setting.trim() != "" || setting.charAt(0) == '#') {
					putSetting(setting, settings);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			displayError("Error with \"data/" + SETTINGS_FILE_NAME + "\"", e);
		}
	}

	/**
	 * Split lines read from the bot's settings file and split them, storing
	 * it as a pair into a settings hashmap
	 *
	 * @param line The line read from the settings file to split and store
	 */
	private static void putSetting(String line, HashMap<String, String> map) {
		String[] pair = line.split(":");
		pair[0] = pair[0].trim();
		pair[1] = pair[1].trim();
		map.put(pair[0], (pair[1] == null) ? "" : pair[1]);
	}

	/**
	 * Returns a global bot setting parameter value from "bot.settings"
	 *
	 * @param param The setting to get the value for
	 * @return The value of the setting
	 */
	public static String getParameter(String param) {
		return settings.get(param);
	}

	/**
	 * Returns a server setting value
	 *
	 * @param serverID The ID of the server
	 * @param param The setting to get the value for
	 * @return The value of the setting
	 */
	public static String getServerParameter(String serverID, String param) {
		return serverSettings.get(param);
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

	public static Map<String, String> readServerSetting(String serverID, String setting) {
		String file = "data/Servers/" + serverID + "/bot.settings";
		try {
			settings = new HashMap<String, String>();
			Scanner reader = new Scanner(new File(file));

			while (reader.hasNext()) {
				//putSetting(reader.nextLine());
			}
			reader.close();
		} catch (FileNotFoundException e1) {

		} catch (Exception e2) {}
		

		return null;
	}

	/**
	 * Writes and saves a server parameter setting for a
	 * specified server
	 * 
	 * @param serverID The ID of the server
	 * @param setting The parameter to write
	 * @param value The value of the setting to write
	 */
	public static void writeServerSetting(String serverID, String setting, String value) {
		String filePath = "data/Servers/" + serverID + "/bot.settings";
		try {
			File settingsFile = new File(filePath);
			Scanner reader = new Scanner(settingsFile);
			FileWriter writer = new FileWriter(settingsFile);
			boolean written = false;

			while (reader.hasNext()) {
				String line = reader.nextLine();
				// Overwrite previous value
				if (line.startsWith(setting + ":")) {
					writer.write(setting + ": " + value);
					written = true;
				} else {
					writer.write(line);
				}
			}
			reader.close();

			// Write setting if not in file
			if (!written) {
				writer.write(setting + ": " + value);
			}
			writer.close();
		} catch (FileNotFoundException e1) {
			// Create server settings file
			try {
				FileWriter writer = new FileWriter(new File(filePath));
				writer.write(setting + ": " + value);
				writer.close();
			} catch (Exception e) {
				displayError("Failed to create new settings file for server ID" + serverID, e);
			}
		} catch (IOException e2) {
			displayError("Failed to write server ID " + serverID + "'s settings", e2);
		}
	}

	/**
	 * Displays a message in the command line
	 * 
	 * @param message The message to output to the command line
	 */
	public static void displayMessage(String message) {
		displayMessage(message, null, "Green");
	}

	/**
	 * Displays a colored warning message in the command line
	 * 
	 * @param message The warning message to output to the command line
	 */
	public static void displayWarning(String message) {
		displayMessage(message, "Warning", "Yellow");
	}

	/**
	 * Displays a colored error message in the command line
	 * 
	 * @param message The error message to output to the command line
	 */
	public static void displayError(String message) {
		displayMessage(message, "Error", "Red");
	}

	/**
	 * Displays a colored error message in the command line and prints 
	 * out the stack trace of the exception thrown
	 * 
	 * @param message The error message to output to the command line
	 * @param exception The exception thrown
	 */
	public static void displayError(String message, Exception exception) {
		displayError(message);
		exception.printStackTrace();
	}

	/**
	 * Displays a message in the command line
	 * 
	 * @param message The message to output to the command line
	 * @param type The type of message, if specified e.g. "[BOT:TYPE]"
	 * @param color The color of "[BOT:TYPE]"
	 */
	public static void displayMessage(String message, String type, String color) {
		String output = "";
		switch(color) {
			case "Red": 	output += "\033[1;31m[BOT";
							break;
			case "Yellow": 	output += "\033[1;33m[BOT";
							break;
			case "Green": 	output += "\033[1;32m[BOT";
							break;
			case "Cyan": 	output += "\033[1;36m[BOT";
							break;
			case "Blue": 	output += "\033[1;34m[BOT";
							break;
			case "Purple": 	output += "\033[1;35m[BOT";
							break;
			case "White": 	output += "\033[1;37m[BOT";
							break;
			case "Black": 	output += "\033[1;30m[BOT";
							break;
			case "":		output += "\033[1;0m[BOT";
							break;
		}
		if (type != null) {
			output += ":" + type.toUpperCase();
		}
		output += "]" + "\033[1;0m " + message;
		System.out.println("\n" + output);
	}


	/**
	 * Main is the class executed when the bot is launched
	 * and handles loggin in and running the instance(s)
	 * of the bot
	 */
	public static void main(String[] args) {

		displayMessage("Launching...");

		try {
			// Get launch args
			boolean testMode = false;
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-test") ||
					args[i].equals("-t") ||
					args[i].equals("--test") ) {
					// Testing mode
					testMode = true;
				}
			}

			// Read settings
			loadBotSettings();

			// Kill the launch if token or key was not specified in settings file
			String botToken = "";
			String botTrigger = "";
			
			if (testMode) {
				// Load test bot token and trigger instead
				displayWarning("Launching in TEST mode");
				botToken = settings.get("TestToken");
				botTrigger = settings.get("TestCommandTrigger");
				if (botToken.equals("") || botTrigger.equals("")) {
					throw new Exception("Test Bot token or command trigger not found");
				}
			} else {
				if (botToken.equals("") || botTrigger.equals("")) {
					throw new Exception("Bot token or command trigger not found");
				}
			}

			displayMessage("Token and command trigger found. Logging in...");
			bot = new Instance(botToken, botTrigger);
		} catch (Exception e) {
			displayError("Failed to launch bot", e);
			e.printStackTrace();
		}

	}

}
