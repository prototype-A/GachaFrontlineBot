package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.object.entity.channel.Channel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;


public class BotHelp extends Command {

	private final Map<String, Map<String, Command>> commandLists;


	public BotHelp(String command, Map<String, Map<String, Command>> commandLists) {
		super(command);
		this.commandLists = commandLists;
	}

	public void run() {

		// Check for permissions
		if (canIssueBotCommands()) {

			String arg = getAsOneArg();
			String color = Main.getParameter("EmbedHelpColor");
			String title = "";
			String helpMessage = "";

			// List all available commands
			if (arg == null) {
				title = "List of Available Commands:";

				// Iterate over command lists
				Iterator<Map.Entry<String, Map<String, Command>>> listIter = commandLists.entrySet().iterator();
				while (listIter.hasNext()) {
					Map.Entry<String, Map<String, Command>> list = listIter.next();
					helpMessage += list.getKey() + " Commands:\n";

					// List all commands from command list
					Iterator<Map.Entry<String, Command>> cmdListIter = list.getValue().entrySet().iterator();
					while (cmdListIter.hasNext()) {
						Map.Entry<String, Command> commandList = cmdListIter.next();
						try {
							// Module commands
							CommandModule module = (CommandModule)commandList.getValue();
							helpMessage += " " + listHelpCommand(module.getModuleTrigger(), false, false) +
											" " + commandList.getKey() + "\n";
						} catch (Exception e) {
							// Non-module commands
							helpMessage += " " + Main.getKey() +
											commandList.getKey() + "\n";
						}
					}
					helpMessage += "\n";
				}

				helpMessage += "You can also type " + 
								formatHelpCommand("help", "command") + 
								"to get help for a specific command.";
			}

			// Get help for specific command
			else {
				// Get args and remove leading command trigger key if entered
				String[] args = getArgs();
				if (args[0].startsWith(Main.getKey())) {
					args[0] = args[0].substring(Main.getKey().length(), args[0].length());
				}

				if (args.length == 2 && commandLists.get(args[0]) != null) {
					// Module trigger specified
					
				} else {
					// Find command in all command lists
					boolean cmdFound = false;
					Iterator<Map.Entry<String, Map<String, Command>>> listIter = commandLists.entrySet().iterator();
					while (listIter.hasNext()) {
						Map.Entry<String, Map<String, Command>> list = listIter.next();
						if (list.getValue().containsKey(arg)) {
							try {
								// Module command
								CommandModule module = (CommandModule)list.getValue().get(arg);
								title = "Usage for **'" +
												module.getModuleTrigger() +
												" " + arg + "'** command:";
								helpMessage = list.getValue().get(arg).getHelp();
								cmdFound = true;
								break;
							} catch (Exception e) {
								// Non-module command
								title = "Usage for **'" + arg +
												"'** command:";
								helpMessage = list.getValue().get(arg).getHelp();
								cmdFound = true;
								break;
							}
						}
					}

					// Command not found
					if (!cmdFound) {
						sendTempMessage(spec -> {
											spec.setColor(Color.of(Integer.parseInt(Main.getParameter("EmbedFailureColor"))))
												.setDescription("Command '" + arg + "' not found");
										});
					}
				}
			}

			// Send message
			Color embedColor = Color.of(Integer.parseInt(color));
			if (this.COMMAND.equals("to-channel")) {
				// To guild
				String embedTitle = title;
				String embedBody = helpMessage;
				sendMessage(spec -> spec.setTitle(embedTitle)
										.setColor(embedColor)
										.setDescription(embedBody));
			}
			/*
			else if (this.COMMAND.equals("to-dm")) {
				// To direct messages
				sendDirectMessage(embeddedMessage, cmdMessage.getAuthor().get());
				if (cmdMessage.getType() == Channel.Type.GUILD_TEXT) {
					sendTempMessage("Help was sent to your DM!");
				}
			}
			*/
		}
	}

	/**
	 * Returns the command with the trigger formatted for
	 * listing
	 *
	 * @param command The command issued
	 * @param bold Applies bold to the command if True
	 * @param singleQuote Applies single quotes to the command if True
	 * @return The formatted command
	 */
	private static String listHelpCommand(String command, boolean bold,
											boolean singleQuote) {
		return ((singleQuote) ? "'" : "") + ((bold) ? "**" : "") +
				Main.getKey() + command +
				((bold) ? "**" : "") + ((singleQuote) ? "'" : " ");
	}

	/**
	 * Returns the command formatted for help messages
	 *
	 * @param command The command issued
	 * @return The formatted command
	 */
	public static String formatHelpCommand(String command) {
		return listHelpCommand(command, true, true);
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

		return listHelpCommand(command, true, true) + "*" + params + "*' ";
	}

	/**
	 * Returns a formatted help message of a specified command
	 *
	 * @param command The command issued
	 * @param helpMsg The help message of the command
	 * @return The formatted help message
	 */
	public static String formatHelpMessage(String command, String helpMsg) {
		return formatHelpCommand(command) + "\n" + helpMsg + "\n";
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

		return formatHelpCommand(command, params) + "\n" + helpMsg + "\n";
	}

	/**
	 * Returns a formatted help message of a specified sub-command
	 * in a main command module
	 *
	 * @param command The main command issued
	 * @param subcommand The sub-command issued
	 * @return The formatted command and subcommand string
	 */
	public static String formatModuleHelpCommand(String command,
													String subcommand) {
		return "'**" + Main.getKey() + command + " " + subcommand + "**'\n";
	}

	/**
	 * Returns a formatted help message of a specified sub-command
	 * in a main command module
	 *
	 * @param command The main command issued
	 * @param subcommand The sub-command issued
	 * @param helpMsg The help message of the sub-command
	 * @return The formatted help message
	 */
	public static String formatModuleHelpMessage(String command,
													String subcommand,
													String helpMsg) {
		return formatModuleHelpCommand(command, subcommand).replace("\n", "") +
				helpMsg + "\n";
	}

	/**
	 * Returns a formatted help message of a specified sub-command
	 * in a main command module
	 *
	 * @param command The main command issued
	 * @param subcommand The sub-command issued
	 * @param params The possible parameters of the sub-command
	 * @param helpMsg The help message of the sub-command
	 * @return The formatted help message
	 */
	public static String formatModuleHelpMessage(String command,
													String subcommand,
													String params,
													String helpMsg) {
		if (params == null || params.equals("")) {
			return formatModuleHelpMessage(command, subcommand, helpMsg);
		}

		return formatModuleHelpCommand(command, subcommand).replace("'\n", "") +
				" *" + params + "*'\n" + helpMsg + "\n";
	}

	public String getHelp() {
		String helpMessage = "";
		if (this.COMMAND.equals("to-dm")) {
			helpMessage += formatHelpMessage("pmhelp", "Sends a message to your DMs containing help regarding commands");
		} else {
			helpMessage += formatHelpMessage("help", "Displays the list of available commands") + "\n";
			helpMessage += formatHelpMessage("help", "command", "Displays help for a specific command");
		}

		return helpMessage;
	}

}
