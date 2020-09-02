package com.prototypeA.discordbot.GachaFrontline_Bot;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;


public abstract class BotEmbedMessage extends BotMessage {

	protected final String[] NAV;
	protected final GatewayDiscordClient GATEWAY;
	protected final User USR;
	protected Message msg;


	public BotEmbedMessage(String[] btns, GatewayDiscordClient gtwy,
							Message msg, User usr) {
		this.NAV = btns;
		this.GATEWAY = gtwy;
		this.USR = usr;
		this.msg = msg;

		gateway.on(ReactionAddEvent.class).subscribe();
		addEmojisToMessage(this.msg, this.NAV);
	}

}
