package com.prototypeA.discordbot.GachaFrontline_Bot;

import java.util.Map;
import java.util.TreeMap;


public abstract class CommandModule extends Command {

	protected final String MODULE_NAME;
	protected final String MODULE_TRIGGER;
	protected final String SUBCOMMAND;
	protected final Map<String, Command> COMMANDS;


	public CommandModule(String name, String trigger) {
		super(null);
		this.MODULE_NAME = name;
		this.MODULE_TRIGGER = trigger;
		this.SUBCOMMAND = null;
		this.COMMANDS = new TreeMap<>();
	}

	public CommandModule(String name, String trigger, String command,
							String subcommand) {
		super(command);
		this.MODULE_NAME = name;
		this.MODULE_TRIGGER = trigger;
		this.SUBCOMMAND = subcommand;
		this.COMMANDS = null;
	}

	public String getModuleName() {
		return this.MODULE_NAME;
	}

	public String getModuleTrigger() {
		return this.MODULE_TRIGGER;
	}

	public Map<String, Command> getCommandList() {
		return this.COMMANDS;
	}

	public String getSubcommand() {
		return this.SUBCOMMAND;
	}
}
