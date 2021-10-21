package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.common.JacksonResources;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.lifecycle.ReconnectEvent;
import discord4j.core.event.domain.lifecycle.ReconnectFailEvent;
import discord4j.core.event.domain.lifecycle.ReconnectStartEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;

import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Instance {

	private final String BOT_TOKEN; // The bot's login token
	private final String CMD_TRIGGER; // The symbol(s) to invoke bot commands
	private final DiscordClient CLIENT;
	private final GatewayDiscordClient GATEWAY;

	private static Map<String, Command> pmCommands;
	private static Map<String, Command> guildCommands;


	public Instance(String token, String trigger) {
		// Set bot parameters
		BOT_TOKEN = token;
		CMD_TRIGGER = trigger;

		// Initialize bot commands
		initCommands();

		ConsoleUtils.printMessage("Logging in...");
		
		// Attempt to log in
		CLIENT = DiscordClientBuilder.create(BOT_TOKEN).build();
		GATEWAY = CLIENT
					//.gateway()
					//.setEnabledIntents(IntentSet.all())
					.login()
					.block();

		// Register application commands
		registerCommands(GATEWAY.getRestClient());

		// Subscribe to Discord events
		subscribeToEvents();

		ConsoleUtils.printMessage("Login successful. Bot Ready.");

		// Block until client gateway disconnects
		GATEWAY.onDisconnect().block();
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
				String cmd = command.getFullName();

				Map<String, String> aliases = command.getAliases();

				ConsoleUtils.printMessage("Found " + (commands.size() + 
					aliases.size()) + " commands");

				for (Command.CommandType type: command.COMMAND_TYPES) {
					if (type == Command.CommandType.GUILD) {
						guildCommands.put(cmd, command);
					} else if (type == Command.CommandType.PM) {
						pmCommands.put(cmd, command);
					}
				}

				ConsoleUtils.printMessage("Loaded \"" + cmd + "\" command");

				// Load command aliases
				for (String key: aliases.keySet()) {
					String subCommand = aliases.get(key);
					command = commandClass.getDeclaredConstructor(String.class, 
								String.class).newInstance(key, subCommand);
					cmd = command.getFullName();

					for (Command.CommandType type: command.COMMAND_TYPES) {
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
	}


    private boolean commandChanged(ApplicationCommandData discordCommand, 
									ApplicationCommandRequest command) {
        // Check if description has changed.
        if (!discordCommand.description()
			.equals(command.description().toOptional().orElse("")))
			return true;

        // Check if default permissions have changed
        boolean discordCommandDefaultPermission = discordCommand.defaultPermission()
													.toOptional().orElse(true);
        boolean commandDefaultPermission = command.defaultPermission()
											.toOptional()
											.orElse(true);
        if (discordCommandDefaultPermission != commandDefaultPermission)
			return true;

		// Check and return if options have changed.
        return !discordCommand.options().equals(command.options());
    }

	/**
	 * Register the bot's commands with Discord as (chat) application 
	 * commands so that they show up as available "slash" commands 
	 * when the user types '/'
	 *
	 * @param restClient The REST client of the bot's gateway
	 */
	private void registerCommands(RestClient restClient) {
		final JacksonResources d4jMapper = JacksonResources.create();
		final ApplicationService applicationService = restClient.getApplicationService();
		final long applicationId = restClient.getApplicationId().block();

		// Commands already registered with discord from previous runs of the bot
		Map<String, ApplicationCommandData> discordCommands = applicationService
			.getGlobalApplicationCommands(applicationId)
			.collectMap(ApplicationCommandData::name)
			.block();

		// Get guild command jsons
		Map<String, ApplicationCommandRequest> commandJsons = new HashMap<>();
		for (String key : guildCommands.keySet()) {
			Command command = guildCommands.get(key);
			String commandJson = command.generateCommandJson();
			try {
				ApplicationCommandRequest request = d4jMapper.getObjectMapper()
					.readValue(commandJson, ApplicationCommandRequest.class);

				commandJsons.put(request.name(), request);

				// Check if new command that has not already been registered
				if (!discordCommands.containsKey(request.name())) {
					applicationService.createGlobalApplicationCommand(applicationId, 
						request).block();

					ConsoleUtils.printMessage("Created global command \"" + 
						request.name() + "\"");
				}
			} catch (Exception e) {
				ConsoleUtils.printError("Failed to register application command \"" + 
					command.getFullName() + "\"");
				e.printStackTrace();
			}
		}

		// Check if any commands have been deleted or changed.
		for (ApplicationCommandData discordCommand : discordCommands.values()) {
			long discordCommandId = Long.parseLong(discordCommand.id());

			ApplicationCommandRequest command = commandJsons.get(discordCommand.name());

			if (command == null) {
				// Removed command.json, delete global command
				applicationService.deleteGlobalApplicationCommand(applicationId, 
					discordCommandId).block();

				ConsoleUtils.printMessage("Deleted global command \"" + 
					discordCommand.name() + "\"");
				continue; // Skip further processing on this command.
			}

			// Check if the command has been changed and needs to be updated.
			if (commandChanged(discordCommand, command)) {
				applicationService.modifyGlobalApplicationCommand(applicationId, 
					discordCommandId, command).block();

				ConsoleUtils.printMessage("Updated global command \"" + 
					command.name() + "\"");
            }
        }
	}


	/**
	 * Subscribes the bot to Discord4J events
	 */
	private void subscribeToEvents() {
		ConsoleUtils.printMessage("Starting Discord event subscription...");

		// When a message is sent by a user
		GATEWAY.on(MessageCreateEvent.class).subscribe(event -> {
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
					botCommand.init(GATEWAY, message);
					Thread commandThread = new Thread(botCommand);
					commandThread.start();
				}
			}
		});

		GATEWAY.on(DisconnectEvent.class).subscribe(event -> {
			ConsoleUtils.printWarning("Gateway connection interrupted.");
		});

		GATEWAY.on(ReadyEvent.class).subscribe(event -> {
			ConsoleUtils.printMessage("Bot Ready.");
		});

		GATEWAY.on(ReconnectFailEvent.class).subscribe(event -> {
			ConsoleUtils.printMessage("Reconnection attempt #" + event.getCurrentAttempt() + " failed.");
		});


		// Slash commands
		GATEWAY.on(ChatInputInteractionEvent.class).subscribe(event -> {
			// Acknowledge the interaction
			event.deferReply().block();

			try {
				Command command = guildCommands.get(event.getCommandName());
				command.init(GATEWAY, null);

				// Set parameters passed through interaction
				for (ApplicationCommandInteractionOption option : event.getOptions()) {
					command.setParam(option.getName(), 
						option.getValue().get().asString());
				}
				command.setInteraction(event.getInteraction());

				Thread commandThread = new Thread(command);
				commandThread.start();
			} catch (Exception e) {
				ConsoleUtils.printError("Failed to run execute command \"" + 
					event.getCommandName() + "\" from interaction");
				e.printStackTrace();
			}

			// Delete the "user used /command" message
			event.deleteReply().block();
		});

		ConsoleUtils.printMessage("Event subscription completed.");
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
	private String getCommand(String message) {
		return getCommand(message, false);
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
}
