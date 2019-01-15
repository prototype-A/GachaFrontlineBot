package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.internal.json.objects.EmbedObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;


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

			EmbedObject embeddedMessage = JsonEmbed.embedMessage(helpMessage, color);

			// Check whether to send to guild channel or user PM (after being called from a guild)
			if (this.command.equals("topm")) {
				cmdMessage.getAuthor().getOrCreatePMChannel().sendMessage(embeddedMessage);
				sendTempMessage("Help was sent to your DM!");
			} else if (this.command.equals("tochannel")) {
				sendMessage(embeddedMessage);
			}
		}
	}

	public String getHelp() {
		String helpMessage = "";
		if (this.command.equals("topm")) {
			helpMessage += "Sends a message to your PMs containing help regarding commands";
		} else {
			helpMessage += formatHelpMessage("help", "Displays the list of available commands");
			helpMessage += formatHelpMessage("help", "command", "Displays help for a specific command");
		}

		return helpMessage;
	}

}
