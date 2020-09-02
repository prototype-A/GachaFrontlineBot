package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.object.entity.channel.Channel;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;


public class BotHelp extends Command {

	private static Map<String, Map<String, Command>> commandLists;


	public BotHelp(String command, Map<String, Map<String, Command>> commandLists) {
		this.command = command;
		this.commandLists = commandLists;
	}

	public void run() {

		// Check for permissions
		if (canIssueBotCommands()) {

			String arg = getAsOneArg();
			String color = Main.getParameter("EmbedHelpColor");
			String helpMessage = "";

			if (arg == null) {
				// List all available commands
				helpMessage = "List of Available Commands:\n\n";

				// Iterate over command lists
				Iterator<Map.Entry<String, Map<String, Command>>> listIter = commandLists.entrySet().iterator();
				while (listIter.hasNext()) {
					Map.Entry<String, Map<String, Command>> list = listIter.next();
					helpMessage += list.getKey() + " Commands:\n";

					Iterator<String> cmdIter = list.getValue().keySet().iterator();
					while (cmdIter.hasNext()) {
						helpMessage += " " + Main.getKey() + cmdIter.next() + "\n";
					}
					helpMessage += "\n";
				}

				helpMessage += "\nYou can also type " + 
								formatHelpCommand("help", "command") + 
								"to get help for a specific command.";
			} else {
				// Remove leading command trigger key
				if (arg.startsWith(Main.getKey())) {
					arg = arg.substring(Main.getKey().length(), arg.length());
				}

				// Get help for specific command
				try {
					// Find command in command lists
					Iterator<Map.Entry<String, Map<String, Command>>> listIter = commandLists.entrySet().iterator();
					while (listIter.hasNext()) {
						Map.Entry<String, Map<String, Command>> list = listIter.next();
						if (list.getValue().containsKey(arg)) {
							helpMessage = "Usage for " + formatHelpCommand(arg) + "command:\n\n";
							helpMessage += list.getValue().get(arg).getHelp();
							break;
						}
					}
				} catch (Exception e) {
					helpMessage = "Command '" + arg + "' not found";
					color = Main.getParameter("EmbedFailureColor");
				}
			}

			// Send message
			Consumer <? super EmbedCreateSpec> embeddedMessage = JsonEmbed.embedMessage(helpMessage, color);
			/*
			// Check whether to send to guild channel or user PM (after being called from a guild)
			if (this.command.equals("to-dm")) {
				sendDirectMessage(embeddedMessage, cmdMessage.getAuthor().get());
				if (cmdMessage.getType() == Channel.Type.GUILD_TEXT) {
					sendTempMessage("Help was sent to your DM!");
				}
			} else
			*/
			if (this.command.equals("to-channel")) {
				sendMessage(embeddedMessage);
			}
		}
	}

	/**
	 * Returns the command formatted for help messages
	 *
	 * @param command The command issued
	 * @return The formatted command
	 */
	public static String formatHelpCommand(String command) {
		return "'**" + Main.getKey() + command + "**' ";
	}

	/**
	 * Returns the command and its possible parameters
	 * formatted for help messages
	 *
	 * @param command The command issued
	 * @param params The possible parameters of the command
	 * @return The formatted command and command parameter string
	 */
	public static String formatHelpCommand(String command, String params) {
		if (params == null || params.equals("")) {
			return formatHelpCommand(command);
		}

		return "'**" + Main.getKey() + command + "** *" + params + "*' ";
	}

	/**
	 * Returns a formatted help message of a specified command
	 *
	 * @param command The command issued
	 * @param helpMsg The help message of the command
	 * @return The formatted help message
	 */
	public static String formatHelpMessage(String command, String helpMsg) {
		return formatHelpCommand(command) + " - " + helpMsg + "\n";
	}

	/**
	 * Returns a formatted help message of a specified command
	 *
	 * @param command The command issued
	 * @param params The possible parameters of the command
	 * @param helpMsg The help message of the command
	 * @return The formatted help message
	 */
	public static String formatHelpMessage(String command, String params,
											String helpMsg) {
		if (params == null || params.equals("")) {
			return formatHelpMessage(command, helpMsg);
		}

		return formatHelpCommand(command, params) + " - " + helpMsg + "\n";
	}

	/**
	 * Returns a formatted help message of a specified sub-command
	 * in a main command module
	 *
	 * @param command The main command issued
	 * @param subcommand The sub-command issued
	 * @param params The possible parameters of the sub-command
	 * @param helpMsg The help message of the sub-command
	 */
	public static String formatHelpMessage(String command, String subcommand,
											String params, String helpMsg) {
		if (params == null || params.equals("")) {
			return formatHelpMessage(command, subcommand, helpMsg);
		}

		return formatHelpCommand(command) +
				formatHelpMessage(subcommand, params, helpMsg);
	}

	public String getHelp() {
		String helpMessage = "";
		if (this.command.equals("to-dm")) {
			helpMessage += "Sends a message to your DMs containing help regarding commands";
		} else {
			helpMessage += formatHelpMessage("help", "Displays the list of available commands");
			helpMessage += formatHelpMessage("help", "command", "Displays help for a specific command");
		}

		return helpMessage;
	}

}
