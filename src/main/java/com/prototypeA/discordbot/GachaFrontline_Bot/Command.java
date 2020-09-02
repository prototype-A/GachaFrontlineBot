package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.InterruptedException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public abstract class Command extends BotMessage implements Runnable {

	protected String command;
	protected String subcommand;


	protected void init(Message message, GatewayDiscordClient gateway) {
		super.init(message, gateway);
		
	}

	public abstract void run();

	public abstract String getHelp();

	/**
	 * Fetch all arguments passed with the issued bot command
	 * and split on whitespace
	 *
	 * @return A string array containing all arguments passed to the command
	 */
	protected String[] getArgs() {

		// Initialize as no arguments passed
		String[] args = new String[0];

		// Check if there is any spaces => there are arguments passed
		String messageContents = cmdMessage.getContent();
		if (messageContents.contains(" ")) {
			// Store words in message as arguments, splitting on space
            args = messageContents.substring(messageContents.indexOf(' ') + 1).split(" ");
        }

		return args;
	}

	/**
	 * Fetch all arguments passed in the command and returns it as a whole string
	 *
	 * @return All arguments passed to the command as an entire string
	 */
	protected String getAsOneArg() {

		// Initialize as no arguments passed
		String arg = null;

		// Check if there is any spaces => there are arguments passed
		String content = cmdMessage.getContent();
		if (content.contains(" ")) {
            arg = content.substring(content.indexOf(' ') + 1).trim();
        }

		return arg;
	}

	/**
	 * Determine if a user that tried to issue a command has or
	 * does not have a certain role in the server
	 * 
	 * @param setting The name of the role in the server
	 * @return True if the user has the role, False otherwise
	 */
	protected boolean userHasRole(String roleName) {
		try {
			// Fetch the user's roles in the server
			List<Role> roleList = cmdMessage.getGuild()
									.block()
									.getMemberById(cmdMessage.getAuthor()
										.get()
										.getId())
									.block()
									.getRoles()
									.buffer()
									.blockLast();

			// Iterate over the list to check if the person has the valid role
			Iterator<Role> roleIter = roleList.iterator();
			while (roleIter.hasNext()) {
				Role role = roleIter.next();
				if (role.getName().equals(roleName)) {
					return true;
				}
			}
		} catch (Exception e) {}

		return false;
	}

	/**
	 * Check if user has the role specified by 'BotCommandRole'
	 * in data/bot.settings that allows them to issue commands
	 * to the bot. If 'BotCommandRole' is not set, check that
	 * the user does not have the role 'CannotIssueCommandsRole'
	 * 
	 * @return True if user has the role, if 'BotCommandRole' is not set or if user does not have role that prevents them from issuing commands, otherwise False
	 */
	protected boolean canIssueBotCommands() {
		// Role unspecified in bot.settings, return true
		if (Main.getParameter("CanIssueCommandsRole").equals("") ||
			Main.getParameter("CanIssueCommandsRole") == null ||
			Main.getParameter("CannotIssueCommandsRole").equals("") ||
			Main.getParameter("CannotIssueCommandsRole") == null) {
			return true;
		}

		// Check if user has role in guild
		return userHasRole(Main.getParameter("CanIssueCommandsRole")) && !userHasRole(Main.getParameter("CannotIssueCommandsRole"));
	}

	/**
	 * Sends a message when a user with insufficient permissions tries to issue a bot command
	 */
	protected void sendNoPermissionsMessage() {
		sendMessage("You do not have the permissions to use this command.");
	}

	/**
	 * Output a warning to the command line when bot does not have the 
	 * sufficient permissions to perform an action
	 *
	protected void missingPermissionsWarning(MissingPermissionsException e) {
		Iterator<Permissions> missingPermissions = e.getMissingPermissions().iterator();
		String permissionsWarning = "Could not perform an action due to these missing permissions: ";
		while (missingPermissions.hasNext()) {
			permissionsWarning += missingPermissions.next().name();
			if (missingPermissions.hasNext()) permissionsWarning += ", ";
		}
		permissionsWarning += ".";
		Main.displayWarning(permissionsWarning);
	}
	*/

	/*
	protected IChannel getChannel(long id) {
		Iterator<IChannel> channelIter = cmdMessage.getGuild().getChannels().iterator();

		while (channelIter.hasNext()) {
			IChannel chn = channelIter.next();
			if (chn.getLongID() == id) {
				return chn;
			}
		}

		return null;
	}
	*/

	protected void redoReacts() {}

}
