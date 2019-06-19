package com.keegan.bot.Dude_Guy_Bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class Main {

	/**
	 * Main is the class executed when the bot is launched
	 * and handles loggin in and running the instance(s)
	 * of the bot
	 */

	private static HashMap<String, String> settings;
	private static final String SETTINGS_FILE_NAME = "bot.settings";

	private static final String TOKEN_KEY = "Token";
	private static final String CMD_KEY = "CommandTrigger";

	private static Instance bot;


	/**
	 * Load settings from 'data/bot.settings' into a hashtable
	 */
	private static void loadSettings() {
		try {
			settings = new HashMap<String, String>();
			Scanner reader = new Scanner(new File("data/" + SETTINGS_FILE_NAME));

			while (reader.hasNext()) {
				putSetting(reader.nextLine());
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
	private static void putSetting(String line) {
		String[] pair = line.split(":");
		pair[0] = pair[0].trim();
		pair[1] = pair[1].trim();
		settings.put(pair[0], (pair[1] == null) ? "" : pair[1]);
	}

	/**
	 * Gets a bot setting parameter from "bot.settings"
	 *
	 * @param param The setting to get the value for
	 * @return The value of the setting
	 */
	public static String getParameter(String param) {
		String param = settings.get(param);
		if (param == null) {
			param = readServerParam(param);
		}
		return param;
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

	public static void writeServerSetting(String serverID, String setting, String value) {
		String file = "data/Servers/" + serverID + "/bot.settings";
		try {
			File settingsFile = new File(file);
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
				File createSettings = new File(file);
				FileWriter writer = new FileWriter(createSettings);
				writer.write(setting + ": " + value);
				writer.close();
			} catch (Exception e2) {
				displayError("Failed to create new settings file for server", e1);
			}
		} catch (IOException e3) {
			
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
	 * Launch the bot
	 */
	public static void main(String[] args) {

		try {
			displayMessage("Launching...");

			// Testing mode
			boolean testing = false;
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-test") || args[i].equals("--test")) {
					testing = true;
				}
			}

			// Read settings file
			loadSettings();

			// Kill the launch if token or key was not specified in settings file
			String botToken = settings.get(TOKEN_KEY);
			String botTrigger = settings.get(CMD_KEY);
			if (botToken.equals("") || botTrigger.equals("")) {
				throw new Exception("Bot token or command trigger not found");
			}

			// Continue launching the bot
			displayMessage("Token and command trigger found");
			if (testing) {
				// Load test bot token and trigger instead
				displayMessage("Launching in TEST mode");
				botToken = settings.get("TestToken");;
				botTrigger = settings.get("TestCommandTrigger");
			}
			bot = new Instance(botToken, botTrigger);

			// Log the bot in
			displayMessage("Logging in...");
			bot.login();
		} catch (Exception e) {
			displayError("Failed to launch bot", e);
		}

	}

}
