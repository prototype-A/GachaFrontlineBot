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

import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Instance {

	private final String BOT_TOKEN; // The bot's login token
	private final String CMD_TRIGGER; // The symbol(s) to invoke bot commands
	private final DiscordClient client;
	private final GatewayDiscordClient gateway;

	private static Map<String, Command> pmCommands;
	private static Map<String, Command> guildCommands;
	//private static Map<String, CommandModule> modules;


	public Instance(String token, String trigger) {
		// Set bot parameters
		BOT_TOKEN = token;
		CMD_TRIGGER = trigger;

		// Initialize bot commands
		initCommands();

		ConsoleUtils.printMessage("Logging in...");
		
		// Attempt to log in
		client = DiscordClient.create(BOT_TOKEN);
		gateway = client.login()
						.block();

		// Subscribe to Discord events
		subscribeToEvents();

		ConsoleUtils.printMessage("Login successful. Bot Ready.");

		// Block until client gateway disconnects
		gateway.onDisconnect().block();
	}

	/**
	 * Subscribes the bot to Discord4J events
	 */
	public void subscribeToEvents() {
		ConsoleUtils.printMessage("Starting Discord event subscription...");

		// When a message is sent by a user
		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			final Message message = event.getMessage();
			String messageContents = message.getContent();
			MessageChannel channel = message.getChannel().block();

			// Check for custom server command trigger
			String guildId = (message.getGuildId().isPresent() ? message.getGuildId().get().asString() : null);
			String trigger = Main.getServerParameter(guildId, "CommandTrigger");
			if (trigger == null) {
				// No custom trigger defined; use default bot trigger
				trigger = CMD_TRIGGER;
			}

			// Check if user tried to issue a bot command
			if (messageContents.startsWith(trigger)) {
				String command = getCommand(messageContents);
				String subcommand = getSubCommand(messageContents);

				// Find command to run
				Command botCommand = null;
				if (channel.getType() == Channel.Type.DM &&
					pmCommands.containsKey(command)) {
					// PM Command
					botCommand = pmCommands.get(command);
				} else if (channel.getType() == Channel.Type.GUILD_TEXT &&
							guildCommands.containsKey(command)) {
					// Guild command
					botCommand = guildCommands.get(command);
				}

				// Run the command if found
				if (command != null) {
					botCommand.init(message);
					Thread commandThread = new Thread(botCommand);
					commandThread.start();
				}
			}
		});

		gateway.on(DisconnectEvent.class).subscribe(event -> {
			ConsoleUtils.printWarning("Gateway connection interrupted.");
		});

		gateway.on(ReadyEvent.class).subscribe(event -> {
			ConsoleUtils.printMessage("Bot Ready.");
		});

		gateway.on(ReconnectFailEvent.class).subscribe(event -> {
			ConsoleUtils.printMessage("Reconnection attempt #" + event.getCurrentAttempt() + " failed.");
		});

		/* Discord will automatically keep check connection after a set period of time
		gateway.on(ReconnectStartEvent.class).subscribe(event -> {
			ConsoleUtils.printMessage("Gateway attempting reconnection...");
		});

		gateway.on(ReconnectEvent.class).subscribe(event -> {
			ConsoleUtils.printMessage("Gateway connection successfully re-established.");
		});
		*/


		ConsoleUtils.printMessage("Event subscription completed.");
	}

	/**
	 * Initializes all of the bot's available commands
	 */
	private void initCommands() {
		ConsoleUtils.printMessage("Loading bot commands...");

		guildCommands = new HashMap<String, Command>();
		pmCommands = new HashMap<String, Command>();

		// Use reflection to find all subclasses of the Command class
		Reflections reflections = new Reflections("com.prototypeA.discordbot.GachaFrontline_Bot");
		Set<Class<? extends Command>> commands = reflections.getSubTypesOf(Command.class);

		for (Class<? extends Command> commandClass: commands) {
			try {
				Command command = commandClass.newInstance();
				String subcommand = command.SUBCOMMAND;
				String cmd = command.COMMAND + (subcommand.equals("") ? "" : " " + subcommand);

				Map<String, String> aliases = command.getAliases();

				ConsoleUtils.printMessage("Found " + (commands.size() + aliases.size()) + " commands");

				for (Command.CommandType type: command.commandTypes) {
					if (type == Command.CommandType.GUILD) {
						guildCommands.put(cmd, command);
					} else if (type == Command.CommandType.PM) {
						pmCommands.put(cmd, command);
					}
				}

				ConsoleUtils.printMessage("Loaded \"" + cmd + "\" command");

				// Load command aliases
				for (String key: aliases.keySet()) {
					subcommand = aliases.get(key);
					cmd = key + (subcommand.equals("") ? "" : " " + subcommand);

					for (Command.CommandType type: command.commandTypes) {
						if (type == Command.CommandType.GUILD) {
							guildCommands.put(cmd, command);
						} else if (type == Command.CommandType.PM) {
							pmCommands.put(cmd, command);
						}
					}

					ConsoleUtils.printMessage("Loaded \"" + cmd + "\" command");
				}
			} catch (Exception e) {
				ConsoleUtils.printError("Failed to load commands from " + commandClass.getCanonicalName());
				e.printStackTrace();
			}
		}

		ConsoleUtils.printMessage("Commands loaded.");

		/*
		Map<String, Map<String, Command>> commandLists = new TreeMap<>();

		// Initialize generic Discord guild commands
		initGuildCommands(commandLists);

		// Initialize generic Discord DM Commands
		//initPmCommands(commandLists);

		// Add module commands
		modules = new TreeMap<>();
		modules.put(Main.getParameter("GFLCommand"), new BotGFL());
		modules.put(Main.getParameter("E7Command"), new BotE7());
		modules.put(Main.getParameter("HI3Command"), new BotHI3());
		initModuleCommands(commandLists);
		*/
	}

	/**
	 * Private message commands
	 */
	private void initPmCommands() {
		/*
		pmCommands = new HashMap<String, Command>();
		pmCommands.put("help", new BotHelp("to-dm", guildCommands));
		*/
  	}

	/**
	 * Guild (Server) commands
	 */
	private void initGuildCommands(Map<String, Map<String, Command>> commandLists) {

		/*
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
		*/
  	}

	/**
	 * Initialize module command lists
	 */
	private void initModuleCommands(Map<String, Map<String, Command>> commandLists) {
		/*
		Iterator<Map.Entry<String, CommandModule>> moduleIter = modules.entrySet().iterator();
		while (moduleIter.hasNext()) {
			Map.Entry<String, CommandModule> module = moduleIter.next();
			commandLists.put(module.getValue().getModuleName(), module.getValue().getCommandList());
		}
		*/
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
	 * Returns the sub-command used
	 *
	 * @param message The message sent
	 * @return The subcommand issued to the bot
	 */
	private String getSubCommand(String message) {
		return getCommand(message, true);
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
