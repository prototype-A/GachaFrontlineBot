package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.internal.json.objects.EmbedObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

//import sx.blah.discord.handle.obj.*;


public class BotHelp extends Command {


	public BotHelp(String command) {
		this.command = command;
	}

	public void run() {

		// Check for permissions
		if (canIssueBotCommands()) {

			String arg = getAsOneArg();

			String helpMessage = "";
			// No command specified
			if (arg == null) {
				helpMessage = "List of commands:\n";

				helpMessage += "\n\nYou can also type " + 
					formatHelpMessage(Main.getKey() + "help", "command") + 
					" to get help for a specific command.";
			}

			// Command specified
			else {
				
			}

			EmbedObject embeddedMessage = JsonEmbed.embedMessage(helpMessage, Main.getParameter("EmbedHelpColor"));

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
		return "";
	}

}
