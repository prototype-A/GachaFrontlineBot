package com.prototypeA.discordbot.GachaFrontline_Bot;


public class ConsoleUtils {

	public static String origin = "Bot";

	/**
	 * Outputs a message to the command line
	 * 
	 * @param message The message to output to the command line
	 */
	public static void printMessage(String message) {
		printMessage(message, null, "Green");
	}

	/**
	 * Outputs a colored warning message to the command line
	 * 
	 * @param message The warning message to output to the command line
	 */
	public static void printWarning(String message) {
		printMessage(message, "Warning", "Yellow");
	}

	/**
	 * Outputs a colored error message to the command line
	 * 
	 * @param message The error message to output to the command line
	 */
	public static void printError(String message) {
		printMessage(message, "Error", "Red");
	}

	/**
	 * Outputs a colored error message to the command line and prints 
	 * out the stack trace of the exception thrown
	 * 
	 * @param message The error message to output to the command line
	 * @param exception The exception thrown
	 */
	public static void printError(String message, Exception exception) {
		printError(message);
		exception.printStackTrace();
	}

	/**
	 * Outputs a message to the command line
	 * 
	 * @param message The message to output to the command line
	 * @param msgType The type of message, if specified (e.g. "Error" for "[ORIGIN:ERROR]")
	 * @param color The color of "[ORIGIN:MSG_TYPE]"
	 */
	public static void printMessage(String message, String msgType, String color) {
		String output = "";
		switch(color) {
			case "Red": 	output += "\033[1;31m[";
							break;
			case "Yellow": 	output += "\033[1;33m[";
							break;
			case "Green": 	output += "\033[1;32m[";
							break;
			case "Cyan": 	output += "\033[1;36m[";
							break;
			case "Blue": 	output += "\033[1;34m[";
							break;
			case "Purple": 	output += "\033[1;35m[";
							break;
			case "White": 	output += "\033[1;37m[";
							break;
			case "Black": 	output += "\033[1;30m[";
							break;
			case "":		output += "\033[1;0m[";
							break;
		}
		// Add origin
		if (origin == null || origin.equals("")) {
			origin = "Bot";
		}
		output += origin.toUpperCase();

		// Add message type (if specified)
		if (msgType != null && !msgType.equals("")) {
			output += ":" + msgType.toUpperCase();
			msgType = msgType.toLowerCase();
		}
		output += "]" + "\033[1;0m " + message + "\n";

		// Output message
		if (msgType != null && (msgType.equals("err") || msgType.equals("error"))) {
			System.err.println(output);
		} else {
			System.out.println(output);
		}
	}
}
