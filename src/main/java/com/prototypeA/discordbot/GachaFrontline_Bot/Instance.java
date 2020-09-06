package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.lifecycle.ReconnectEvent;
import discord4j.core.event.domain.lifecycle.ReconnectFailEvent;
import discord4j.core.event.domain.lifecycle.ReconnectStartEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Instance {

	private final String TOKEN; // The bot's login token
	private final String CMD_TRIGGER; // The symbol(s) to invoke bot commands
	private final DiscordClient client;
	private final GatewayDiscordClient gateway;
	private static Map<String, Command> pmCommands;
	private static Map<String, Command> guildCommands;
	private static Map<String, CommandModule> modules;


	public Instance(String token, String trigger) {
		// Set bot parameters
		TOKEN = token;
		CMD_TRIGGER = trigger;

		// Initialize bot commands
		initCommands();
		
		// Attempt to log in
		client = DiscordClient.create(TOKEN);
		gateway = client.login()
						.block();
		Main.displayMessage("Login successful.");

		// Subscribe to events
		subscribeToEvents();
	}

	/**
	 * Subscribes the bot to Discord4J events
	 */
	public void subscribeToEvents() {
		Main.displayMessage("Starting event subscription...");

		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			final Message message = event.getMessage();
			String messageContents = message.getContent();

			// Check if user tried to issue a bot command
			if (messageContents.startsWith(CMD_TRIGGER)) {
				// Validate issued bot command
				String command = getCommand(messageContents);
				String subcommand = getCommand(messageContents, true);
				MessageChannel channel = event.getMessage()
												.getChannel()
												.block();

				Thread commandThread = null;

				// Find command to run
				if (channel.getType() == Channel.Type.DM &&
					pmCommands.containsKey(command)) {
					// PM Command
					Command pmCommand = pmCommands.get(command);
					pmCommand.init(message, gateway);
					commandThread = new Thread(pmCommand);
				} else if (guildCommands.containsKey(command) && channel != null) {
					// Valid generic guild command
					Command guildCommand = guildCommands.get(command);
					guildCommand.init(message, gateway);
					commandThread = new Thread(guildCommand);
				} else {
					// Search for command in modules
					try {
						Command moduleCommand = modules.get(command)
														.getCommandList()
														.get(subcommand);
						moduleCommand.init(message, gateway);
						commandThread = new Thread(moduleCommand);
					} catch (Exception e) {}
				}

				// Run the command if found
				if (commandThread != null) {
					commandThread.start();
				} else {
					Main.displayMessage("Command " + command + " " +
										subcommand + " not found");
				}
			}
		});

		gateway.on(DisconnectEvent.class).subscribe(event -> {
			Main.displayWarning("Gateway connection interrupted.");
		});

		gateway.on(ReadyEvent.class).subscribe(event -> {
			Main.displayMessage("Bot Ready.");
		});

		gateway.on(ReconnectEvent.class).subscribe(event -> {
			Main.displayMessage("Gateway connection successfully re-established.");
		});

		gateway.on(ReconnectFailEvent.class).subscribe(event -> {
			Main.displayMessage("Reconnection attempt #" + event.getCurrentAttempt() + " failed.");
		});

		gateway.on(ReconnectStartEvent.class).subscribe(event -> {
			Main.displayMessage("Gateway attempting reconnection...");
		});


		Main.displayMessage("Event subscription completed.");


		// Block until client gateway disconnects
		gateway.onDisconnect().block();
	}

	/**
	 * Initializes all of the bot's available commands
	 */
	private void initCommands() {
		Map<String, Map<String, Command>> commandLists = new TreeMap<>();

		// Iniialize generic Discord guild commands
		initGuildCommands(commandLists);

		// Initialize generic Discord DM Commands
		//initPmCommands(commandLists);

		// Add module commands
		modules = new TreeMap<>();
		modules.put(Main.getParameter("GFLCommand"), new BotGFL());
		initModuleCommands(commandLists);
	}

	/**
	 * Private message commands
	 *
	private void initPmCommands() {
		pmCommands = new HashMap<String, Command>();
		pmCommands.put("help", new BotHelp("to-dm", guildCommands));
  	}
	*/

	/**
	 * Guild (Server) commands
	 */
	private void initGuildCommands(Map<String, Map<String, Command>> commandLists) {
		// Initialize command lists
		guildCommands = new TreeMap<String, Command>();
		commandLists.put("Discord", guildCommands);

		// Generic Discord commands
		guildCommands.put("avatar", new BotMisc("avatar"));
		guildCommands.put("exit", new BotSystem("exit"));
		guildCommands.put("goto", new BotSystem("goto"));
		guildCommands.put("help", new BotHelp("to-channel", commandLists));
		//guildCommands.put("player", new BotAudio("player"));
		//guildCommands.put("pmhelp", new BotHelp("to-dm", guildCommands));
		//guildCommands.put("queue", new BotAudio("queue"));
		//guildCommands.put("quote", new BotMisc("quote"));
  	}

	/**
	 * Initialize module command lists
	 */
	private void initModuleCommands(Map<String, Map<String, Command>> commandLists) {
		Iterator<Map.Entry<String, CommandModule>> moduleIter = modules.entrySet().iterator();
		while (moduleIter.hasNext()) {
			Map.Entry<String, CommandModule> module = moduleIter.next();
			commandLists.put(module.getValue().getModuleName(), module.getValue().getCommandList());
		}
	}

	/**
	 * Returns the main or sub-command used
	 *
	 * @param message The message sent
	 * @param getSubCommand If true, will return the command after the main command
	 * @return The command issued to the bot
	 */
	private String getCommand(String message, boolean getSubCommand) {
		message = message.toLowerCase();
		int firstSpaceIndex = message.indexOf(" ");
		int secondSpaceIndex = message.indexOf(" ", firstSpaceIndex + 1);

		// Return sub-command
		if (getSubCommand && secondSpaceIndex != -1) {
			// Has arguments
			return message.substring(firstSpaceIndex + 1, secondSpaceIndex);
		} else if (getSubCommand && secondSpaceIndex == -1) {
			// No arguments
			return message.substring(firstSpaceIndex + 1, message.length());
		}

		// Return just the main command
		if (firstSpaceIndex == -1) {
			return message.substring(CMD_TRIGGER.length(), message.length());
		}

		// Return main command
		return message.substring(CMD_TRIGGER.length(), firstSpaceIndex);
	}

	/**
	 * Returns the main command used
	 *
	 * @param message The message sent
	 * @return The command issued to the bot
	 */
	public String getCommand(String message) {
		return getCommand(message, false);
	}
}
