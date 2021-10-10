package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.rest.util.Color;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.InterruptedException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;


public abstract class Command extends Voice implements Runnable {

	public enum CommandType { GUILD, PM };

	public final String COMMAND;
	public final String SUBCOMMAND;
	public final CommandType[] COMMAND_TYPES;
	public final String DESCRIPTION;
	public final CommandParameter[] PARAMETERS;
	protected Map<String, String> aliases;


	public Command(String command, String subcommand, CommandType[] commandTypes,
					String desc, CommandParameter[] parameters) {
		this.COMMAND = command;
		this.SUBCOMMAND = subcommand;
		this.COMMAND_TYPES = commandTypes;
		this.DESCRIPTION = desc;
		this.PARAMETERS = parameters;
		aliases = new HashMap<String, String>();
	}


	/**
	 * Adds an alias to which the command can also be called by
	 *
	 * @param command The (potential) alias for the main command
	 * @param subcommand The (potential) alias for the sub command
	 */
	protected void addAlias(String command, String subcommand) {
		aliases.put(command, subcommand);
	}

	/**
	 * Gets the list of aliases for this command
	 *
	 * @return The list of command/subcommand aliases for this command
	 */
	public Map<String, String> getAliases() {
		return aliases;
	}

	/**
	 * The functionality of the command when it is executed
	 */
	public abstract void run();

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
		String messageContents = commandMessage.getContent();
		if (messageContents.contains(" ")) {
			// Store words in message as arguments, splitting on space
            args = messageContents.substring(messageContents.indexOf(' ') + 1).split(" ");
        }

		return args;
	}

	/**
	 * Fetch all arguments passed in the command and returns it
	 * as a whole string
	 *
	 * @param module Determines whether to strip the second argument (subcommand) or not
	 * @return All arguments passed to the command as an entire string
	 */
	protected String getAsOneArg(boolean module) {
		// Initialize as no arguments passed
		String arg = null;

		// Check if there is any spaces => there are arguments passed
		String content = commandMessage.getContent();
		if (content.contains(" ")) {
			int firstSpaceIndex = content.indexOf(" ");
			int secondSpaceIndex = content.indexOf(" ", firstSpaceIndex + 1);
			if (!module || secondSpaceIndex == -1) {
				// Remove only first word (command)
            	arg = content.substring(firstSpaceIndex + 1).trim();
			} else {
				// Remove second word as well (subcommand)
				arg = content.substring(secondSpaceIndex).trim();
			}
        }

		return arg;
	}

	/**
	 * Fetch all arguments passed in the command and returns it
	 * as a whole string
	 *
	 * @return All arguments passed to the command as an entire string
	 */
	protected String getAsOneArg() {
		return getAsOneArg(false);
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
			List<Role> roleList = commandMessage.getGuild()
									.block()
									.getMemberById(commandMessage.getAuthor()
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
	 * Sends a message when a user with insufficient permissions
	 * tries to issue a bot command
	 */
	protected void sendNoPermissionsMessage() {
		sendMessage("You do not have the permissions to use this command.");
	}

	/**
	 * Reads in a specified JSON file from the same directory as the
	 * executable
	 * 
	 * @param fileName The name of the .json file (including subpath in executable directory) to load
	 */
	protected JSONObject loadJsonFile(String fileName) {
		String dataPath = System.getProperty("user.dir");
		try {
			// Load data
			return new JSONObject(new Scanner(new File(dataPath + fileName)).useDelimiter("\\A").next());
		} catch (FileNotFoundException e) {
			ConsoleUtils.printError("File \"" + dataPath + fileName + "\" does not exist");
		} catch (Exception e) {
			ConsoleUtils.printError(e.getMessage() + " occurred while attempting to read the data");
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Copies and builds the actual EmbedCreateSpec to be sent
	 * as an embed message, due to issues with sending the
	 * original
	 * 
	 * @param newEmbed The EmbedCreateSpec to copy
	 *
	protected EmbedCreateSpec buildEmbedCreateSpec(EmbedCreateSpec newEmbed) {

		// Get embed data
		EmbedData newEmbedData = newEmbed.asRequest();

		return spec -> {
			// Base embed
			spec.setTitle(newEmbedData.title().get())
				.setUrl(newEmbedData.url().get())
				.setDescription(newEmbedData.description().get())
				.setColor(Color.of(newEmbedData.color().get()))
				.setThumbnail(newEmbedData.thumbnail().get().url().get())
				.setImage(newEmbedData.image().get().url().get())
				.setFooter(newEmbedData.footer().get().text(), null);

			// Additional fields
			Iterator<EmbedFieldData> fieldIter = newEmbedData.fields().get().iterator();
			while (fieldIter.hasNext()) {
				EmbedFieldData field = fieldIter.next();
				spec.addField(field.name(), field.value(), field.inline().get());
			}
		};
	}*/

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


	/**
	 * Returns help outlining the usage of this command
	 *
	 * @return The help message of this command
	 */
	public String getHelp() {
		return "";
	}
	
	public String generateCommandJson() {
		String json = "{\n";

		// Name
		json += "\t\"name\": \"" + COMMAND + 
				(SUBCOMMAND.equals("") ? "" : " " + SUBCOMMAND) + "\",\n";

		// Description
		json += "\t\"desc\": \"" + DESCRIPTION + "\",\n";

		// Parameters
		if (PARAMETERS.length > 0) {
			json += "\t\"options\": [\n";

			for (int i = 0; i < PARAMETERS.length; i++) {
				CommandParameter param = PARAMETERS[i];
				json += "\t\t{\n" + 
					"\t\t\t\"name\": \"" + param.getName() + "\",\n" + 
					"\t\t\t\"desc\": \"" + param.getDescription() + "\",\n" + 
					"\t\t\t\"type\": " + param.getType() + ",\n" + 
					"\t\t\t\"required\": " + param.isRequired() + "\n" + 
					"\t\t}";

				if (i < PARAMETERS.length - 1) {
					json += ",\n";
				}
			}

			json += "\n\t]\n";
		}

		json += "}";

		return json;
	}

}
