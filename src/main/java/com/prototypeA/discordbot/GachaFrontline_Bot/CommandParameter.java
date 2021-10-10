package com.prototypeA.discordbot.GachaFrontline_Bot;


public class CommandParameter {

	private final String NAME;
	private final String DESC;
	private final int TYPE;
	private final boolean REQUIRED;


	public CommandParameter() {
		this.NAME = "";
		this.DESC = "";
		this.TYPE = -1;
		this.REQUIRED = false;
	}

	public CommandParameter(String name, String desc, int type,
							boolean isRequired) {
		this.NAME = name;
		this.DESC = desc;
		this.TYPE = type;
		this.REQUIRED = isRequired;
	}


	/**
	 * Gets the name of this command parameter
	 *
	 * @return The name of this command parameter
	 */
	public String getName() {
		return this.NAME;
	}

	/**
	 * Gets the description of this command parameter
	 *
	 * @return The description of this command parameter
	 */
	public String getDesc() {
		return this.getDescription();
	}

	/**
	 * Gets the description of this command parameter
	 *
	 * @return The description of this command parameter
	 */
	public String getDescription() {
		return this.DESC;
	}

	/**
	 * Gets the type of this command parameter
	 *
	 * @return The type of this command parameter
	 */
	public int getType() {
		return this.TYPE;
	}

	/**
	 * Gets whether or not this parameter is required 
	 * for the execution of the main command
	 *
	 * @return True if this parameter is required for the main command, otherwise False
	 */
	public boolean isRequired() {
		return this.REQUIRED;
	}
}
