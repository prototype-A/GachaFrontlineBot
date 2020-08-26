package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.object.entity.channel.Channel;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Consumer;


public class BotHelp extends Command {

	private static HashMap<String, Command> commandList;


	public BotHelp(String command, HashMap<String, Command> commandList) {
		this.command = command;
		this.commandList = commandList;
	}

	public void run() {

		// Check for permissions
		if (canIssueBotCommands()) {

			String arg = getAsOneArg();
			String color = Main.getParameter("EmbedHelpColor");
			String helpMessage = "";

			// Command not specified
			if (arg == null) {
				helpMessage = "List of Available Commands:\n\n";

				// Iterate over command list
				Iterator<String> cmdIter = new TreeSet<String>(commandList.keySet()).iterator();
				while (cmdIter.hasNext()) {
					helpMessage += " " + Main.getKey() + cmdIter.next() + "\n";
				}

				helpMessage += "\nYou can also type " + 
					formatHelpCommand("help", "command") + 
					"to get help for a specific command.";
			}

			// Command specified
			else {
				// Remove leading command trigger key
				if (arg.startsWith(Main.getKey())) {
					arg = arg.substring(Main.getKey().length(), arg.length());
				}

				try {
					helpMessage = "Usage for " + formatHelpCommand(arg) + "command:\n\n";
					helpMessage += commandList.get(arg).getHelp();
				} catch (Exception e) {
					helpMessage = "Command '" + arg + "' not found";
					color = Main.getParameter("EmbedFailureColor");
				}
			}

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

	public static String formatHelpCommand(String command) {
		return "'**" + Main.getKey() + command + "**' ";
	}

	public static String formatHelpCommand(String command, String params) {
		if (params == null || params.equals("")) {
			return formatHelpCommand(command);
		}

		return "'**" + Main.getKey() + command + "** *" + params + "*' ";
	}

	public static String formatHelpMessage(String command, String helpMsg) {
		return formatHelpCommand(command) + " - " + helpMsg + "\n";
	}

	public static String formatHelpMessage(String command, String params, String helpMsg) {
		if (params == null || params.equals("")) {
			return formatHelpMessage(command, helpMsg);
		}

		return formatHelpCommand(command, params) + " - " + helpMsg + "\n";
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
