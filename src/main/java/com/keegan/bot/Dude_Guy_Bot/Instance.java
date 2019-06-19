package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.ChannelCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageSendEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserRoleUpdateEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.events.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HighPrecisionRecurrentTask;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RateLimitException;

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

	private static volatile IDiscordClient client;
	private final String TOKEN; // The bot's login token
	private final String CMD_TRIGGER; // The symbol(s) to invoke bot commands
	private static List<IGuild> guildList;
	private static HashMap<String, Command> pmCommands;
	private static HashMap<String, Command> guildCommands;


	public Instance(String token, String trigger) {
		TOKEN = token;
		CMD_TRIGGER = trigger;
		initCommands();
	}

	public void login() throws DiscordException {
		client = new ClientBuilder().withToken(TOKEN).withRecommendedShardCount().login();
		client.getDispatcher().registerListener(this);
	}

	private void initCommands() {
		initGuildCommands();
		initPmCommands();
	}

	/**
	 * Private message commands
	 */
	private void initPmCommands() {
		pmCommands = new HashMap<String, Command>();
		pmCommands.put("help", new BotHelp("tochannel", guildCommands));
  	}

	/**
	 * Guild (Server) commands
	 */
	private void initGuildCommands() {
		guildCommands = new HashMap<String, Command>();
		guildCommands.put("avatar", new BotMisc("avatar"));
		guildCommands.put("craft", new BotGF("constructiontimer"));
		//guildCommands.put("equip", new BotGF("equip"));
		//guildCommands.put("equipment", new BotGF("equip"));
		guildCommands.put("exit", new BotSystem("exit"));
		guildCommands.put("fairy", new BotGF("fairy"));
		guildCommands.put("goto", new BotSystem("goto"));
		guildCommands.put("help", new BotHelp("tochannel", guildCommands));
		guildCommands.put("map", new BotGF("map"));
		guildCommands.put("player", new BotAudio("player"));
		guildCommands.put("pmhelp", new BotHelp("topm", guildCommands));
		guildCommands.put("prod", new BotGF("productiontimer"));
		guildCommands.put("queue", new BotAudio("queue"));
		guildCommands.put("quote", new BotMisc("quote"));
		guildCommands.put("tdoll", new BotGF("tdoll"));
		guildCommands.put("t-doll", new BotGF("tdoll"));
		guildCommands.put("timer", new BotGF("timer"));
  	}

	/**
	 * Returns the String command used
	 *
	 * @param message The message sent
	 * @return The command issued to the bot
	 */
	public String getCommand(IMessage message) {
		String content = message.getContent().toLowerCase();
		// Get just the command (not including the parameters)
		int end = content.indexOf(" ");
		if (end == -1) {
			end = content.length();
		}
		return content.substring(CMD_TRIGGER.length(), end);
	}

	/**
	 * Logs the bot out and stops it.
	 */
	public void terminate() {
		try {
			Main.displayMessage("Logging out...");
			client.logout();
		} catch (DiscordException e) {
			Main.displayError("Error while attempting to logging out: " + e.getMessage(), e);
		}
	}


	@EventSubscriber
	public void onReady(ReadyEvent event) {
		/**
	 	 * Bot is ready to respond to commands
		 */
		Main.displayMessage("Login successful! Bot ready.");
	}

	@EventSubscriber
	public void onDisconnect(DisconnectedEvent event) {
		Main.displayWarning("Connection interrupted. Please check your network.");
	}

	@EventSubscriber
	public void onReconnectFailed(ReconnectFailureEvent event) {

		Main.displayWarning("Failed to reconnect " + event.getCurrentAttempt() + " times");

		// Last login attempt
		if (event.isShardAbandoned()) {
			Main.displayWarning("Shard will be abandoned if the next reconnect attempt fails.");
		}
	}

	@EventSubscriber
	public void onReconnectSuccess(ReconnectSuccessEvent event) {
		Main.displayMessage("Connection re-established.");
	}

	@EventSubscriber
	public void onMessage(MessageReceivedEvent event) {
		/**
		 * Handles when a message is sent to the server/to the bot via PM.
		 */
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

}
