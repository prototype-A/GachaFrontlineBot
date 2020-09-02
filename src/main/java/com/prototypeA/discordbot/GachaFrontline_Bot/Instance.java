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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class Instance {

	private final String TOKEN; // The bot's login token
	private final String CMD_TRIGGER; // The symbol(s) to invoke bot commands
	private final DiscordClient client;
	private final GatewayDiscordClient gateway;
	private static Map<String, Command> pmCommands;
	private static Map<String, Command> guildCommands;
	private static Map<String, Command> gflCommands;

	private static final String GFL_COMMAND = Main.getParameter("GFLCommand");


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
				// Remove command symbol and validate issued bot command
				String command = getCommand(messageContents);
				messageContents = messageContents.substring(CMD_TRIGGER.length());
				MessageChannel channel = event.getMessage()
												.getChannel()
												.block();

				Thread commandThread = null;

				// Find command to run
				if (channel.getType() == Channel.Type.DM && pmCommands.containsKey(command)) {
					// PM Command
					Command pmCommand = pmCommands.get(command);
					pmCommand.init(message, gateway);
					commandThread = new Thread(pmCommand);
				} else if (command.equals(GFL_COMMAND) && channel != null) {
					// Valid GFL command
					Command gflCommand = gflCommands.get(getCommand(messageContents, true));
					gflCommand.init(message, gateway);
					commandThread = new Thread(gflCommand);
				} else if (guildCommands.containsKey(command) && channel != null) {
					// Valid generic guild command
					Command guildCommand = guildCommands.get(command);
					guildCommand.init(message, gateway);
					commandThread = new Thread(guildCommand);
				}

				// Run the command if found
				if (commandThread != null) {
					commandThread.start();
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
		initGuildCommands();
		//initPmCommands();
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
	private void initGuildCommands() {
		// Initialize command lists
		guildCommands = new TreeMap<String, Command>();
		gflCommands = new TreeMap<>();
		Map<String, Map<String, Command>> commandLists = new TreeMap<>();
		commandLists.put("Discord", guildCommands);
		commandLists.put("Girls Frontline", gflCommands);

		// Generic Discord commands
		guildCommands.put("avatar", new BotMisc("avatar"));
		guildCommands.put("exit", new BotSystem("exit"));
		guildCommands.put("goto", new BotSystem("goto"));
		guildCommands.put("help", new BotHelp("to-channel", commandLists));
		//guildCommands.put("player", new BotAudio("player"));
		//guildCommands.put("pmhelp", new BotHelp("to-dm", guildCommands));
		//guildCommands.put("queue", new BotAudio("queue"));
		//guildCommands.put("quote", new BotMisc("quote"));

		// Girls Frontline
		gflCommands.put("craft", new BotGFL(GFL_COMMAND, "constructiontimer"));
		//gflCommands.put("equip", new BotGFL(GFL_COMMAND, "equip"));
		//gflCommands.put("equipment", new BotGFL(GFL_COMMAND, "equip"));
		gflCommands.put("fairy", new BotGFL(GFL_COMMAND, "fairy"));
		gflCommands.put("map", new BotGFL(GFL_COMMAND, "map"));
		gflCommands.put("mix", new BotGFL(GFL_COMMAND, "mix"));
		gflCommands.put("prod", new BotGFL(GFL_COMMAND, "productiontimer"));
		gflCommands.put("production", new BotGFL(GFL_COMMAND, "productiontimer"));
		gflCommands.put("tdoll", new BotGFL(GFL_COMMAND, "tdoll"));
		gflCommands.put("t-doll", new BotGFL(GFL_COMMAND, "tdoll"));
		gflCommands.put("timer", new BotGFL(GFL_COMMAND, "timer"));
  	}

	/**
	 * Returns the main or sub-command used
	 *
	 * @param message The message sent
	 * @param getNextCommand If true, will return the (sub-)command after the main command
	 * @return The command issued to the bot
	 */
	private String getCommand(String message, boolean getNextCommand) {
		// Get just the command (not including the parameters)
		message = message.toLowerCase();
		int end = message.indexOf(" ");
		if (end == -1) {
			end = message.length();
		}

		// Return sub-command
		if (getNextCommand) {
			return message.substring(0, end);
		}

		// Return main command
		return message.substring(CMD_TRIGGER.length(), end);
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
