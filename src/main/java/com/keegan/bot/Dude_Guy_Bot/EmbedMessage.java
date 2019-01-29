package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.util.RateLimitException;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;


public abstract class EmbedMessage extends BotMessage {

	protected final String[] NAV;
	protected final IDiscordClient BOT;
	protected final IUser USR;
	protected IMessage msg;


	public EmbedMessage(String[] btns, IDiscordClient bot, IMessage msg, IUser usr) {
		this.NAV = btns;
		this.BOT = bot;
		this.USR = usr;
		this.msg = msg;

		this.BOT.getDispatcher().registerListener(this);
		addEmojisToMessage(this.msg, this.NAV);
	}

}
