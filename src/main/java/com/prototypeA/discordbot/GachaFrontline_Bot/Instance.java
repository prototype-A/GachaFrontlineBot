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
import java.util.Scanner;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class Instance {

	private final String TOKEN; // The bot's login token
	private final String CMD_TRIGGER; // The symbol(s) to invoke bot commands
	private final DiscordClient client;
	private final GatewayDiscordClient gateway;
	private static HashMap<String, Command> pmCommands;
	private static HashMap<String, Command> guildCommands;


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

				// Check if PM command
				if (channel.getType() == Channel.Type.DM && pmCommands.containsKey(command)) {
					pmCommands.get(command).init(message, gateway);
					commandThread = new Thread(pmCommands.get(command));
				}
				// Check if valid guild command
				else if (guildCommands.containsKey(command) && channel != null) {
					guildCommands.get(command).init(message, gateway);
					commandThread = new Thread(guildCommands.get(command));
				}

				if (commandThread != null) {
					commandThread.start();
				}
			}
		});

		gateway.on(DisconnectEvent.class).subscribe(event -> {
			Main.displayWarning("Connection interrupted. Please check your network status.");
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
		guildCommands = new HashMap<String, Command>();
		guildCommands.put("avatar", new BotMisc("avatar"));
		//guildCommands.put("exit", new BotSystem("exit"));
		//guildCommands.put("goto", new BotSystem("goto"));
		guildCommands.put("help", new BotHelp("to-channel", guildCommands));
		//guildCommands.put("player", new BotAudio("player"));
		//guildCommands.put("pmhelp", new BotHelp("to-dm", guildCommands));
		//guildCommands.put("queue", new BotAudio("queue"));
		guildCommands.put("quote", new BotMisc("quote"));

		// Girls Frontline
		//HashMap<String, Command> gflCommands = new HashMap<>();
		//guildCommands.put("craft", new BotGFL("constructiontimer"));
		//guildCommands.put("equip", new BotGFL("equip"));
		//guildCommands.put("equipment", new BotGFL("equip"));
		//guildCommands.put("fairy", new BotGFL("fairy"));
		//guildCommands.put("map", new BotGFL("map"));
		//guildCommands.put("mix", new BotGFL("mix"));
		//guildCommands.put("prod", new BotGFL("productiontimer"));
		//guildCommands.put("tdoll", new BotGFL("tdoll"));
		//guildCommands.put("t-doll", new BotGFL("tdoll"));
		//guildCommands.put("timer", new BotGFL("timer"));
  	}

	/**
	 * Returns the String command used
	 *
	 * @param message The message sent
	 * @return The command issued to the bot
	 */
	public String getCommand(String message) {
		// Get just the command (not including the parameters)
		message = message.toLowerCase();
		int end = message.indexOf(" ");
		if (end == -1) {
			end = message.length();
		}
		return message.substring(CMD_TRIGGER.length(), end);
	}

	/*
	public void onMessage(MessageReceivedEvent event) {
		**
		 * Handles when a message is sent to the server/to the bot via PM.
		 *
		try {
			IMessage message = event.getMessage(); // Gets the message object from the event object
			String messageContents = message.getContent(); // Actual message sent

			// Recieved a message from a server the bot is in
			// Check if user tried to issue a bot command
			if (messageContents.startsWith(CMD_TRIGGER)) {

				messageContents = messageContents.substring(CMD_TRIGGER.length());
				String command = getCommand(message);
				IChannel chn = event.getChannel();

				Thread commandThread = null;

				// Check if PM
				if (chn.isPrivate() && pmCommands.containsKey(command)) {
					pmCommands.get(command).init(message, client);
					commandThread = new Thread(pmCommands.get(command));
				}

				// Check if valid guild command, run if so
				else if (guildCommands.containsKey(command) && !chn.isDeleted()) {
					guildCommands.get(command).init(message, client);
					commandThread = new Thread(guildCommands.get(command));
				}

				if (commandThread != null) {
					commandThread.start();
				}
			}
		}
		catch (Exception e) {
			// Command not found/error occurred
			Main.displayError(e.getMessage() + " occurred when running a command", e);
		}

	}
	*/

}
