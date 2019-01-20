package com.keegan.bot.Dude_Guy_Bot;


public abstract class EmbedMessage extends BotMessage implements Runnable {

	protected final String[] NAV;


	public EmbedMessage(String[] buttons) {
		this.NAV = buttons;
	}

	public void start() {}

	public void run() {}

}
