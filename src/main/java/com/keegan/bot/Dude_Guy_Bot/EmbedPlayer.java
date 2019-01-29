package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackStartEvent;

import java.io.File;


public class EmbedPlayer extends EmbedMessage {

	private static final String[] NAV_EMOJIS = {"▶", "⏸", "⏹", "⏭", "🎼", "🔒", "🔉", "🔊", "🔇"};
	private static final float VOLUME_STEP = 0.1f;
	private AudioPlayer player;
	private float unmuteVolume = 1.0f;
	private boolean accessRestricted;

	public EmbedPlayer(IDiscordClient bot, IMessage msg, IUser usr) {
		super(NAV_EMOJIS, bot, msg, usr);
		this.player = AudioPlayer.getAudioPlayerForGuild(msg.getGuild());
		this.unmuteVolume = getVolume();
		this.accessRestricted = false;
	}

	private static String getPlayerBody(String status) {
		return getPlayerBody(status, "0:00", 100);
	}

	private static String getPlayerBody(String status, String dur, int volume) {
		return "0:00 ●━━━━━━━━━ " + dur + "\nStatus: " + status + "\nVolume: " + volume + "%";
	}

	public static EmbedObject buildNewPlayer() {
		String title = "";
		String body = getPlayerBody("Ready", "0:00", 100);
		return buildPlayer(title, body, Main.getParameter("PlayerInitColor"));
	}

	private static EmbedObject buildPlayer(String title, String desc, String color) {
		return buildPlayer(title, desc, Integer.parseInt(color));
	}

	private static EmbedObject buildPlayer(String title, String desc, int color) {
		EmbedBuilder player = new EmbedBuilder();

		player.withTitle(title);
		player.withDesc(desc);
		player.withColor(color);

		return player.build();
	}

	private static EmbedObject rebuildPlayer(IEmbed oldEmbed, String status, String volume) {
		String desc = oldEmbed.getDescription();
		if (status != null) {
			
		} else if (volume != null) {
			
		}

		return buildPlayer(oldEmbed.getTitle(), desc, oldEmbed.getColor().getRGB());
	}

	private void updateDisplay() {
		//this.msg = this.msg.edit(rebuildEmbed(this.msg.getEmbeds().get(0)));
	}

	private void setPause(boolean pause) {
		if (player.getCurrentTrack() != null) {
			player.setPaused(pause);
		}
	}

	private void resumePlayer() {
		setPause(false);
		this.msg = this.msg.edit(rebuildPlayer(this.msg.getEmbeds().get(0), "Playing", null));
	}

	private void pausePlayer() {
		setPause(true);
		this.msg = this.msg.edit(rebuildPlayer(this.msg.getEmbeds().get(0), "Paused", null));
	}

	private void stopPlayer() {
		player.clear();
		//this.msg = this.msg.edit(buildPlayer("", "Stopped", null));
	}

	private void skip() {
		player.skip();
	}

	private void displayQueue() {
		
	}

	private void restrictAccess(boolean restrict) {
		accessRestricted = restrict;

		if (accessRestricted) {
			
		} else {
			
		}
	}

	private void mute() {
		this.unmuteVolume = getVolume();
		this.setVolume(0.0f);
	}

	private float getVolume() {
		return player.getVolume();
	}

	private void setVolume(float volume) {
		player.setVolume(volume);
		//this.msg = this.msg.edit();
	}

	private void increaseVolume() {
		if (getVolume() == 0.0f) {
			// Unmute volume if muted
			setVolume(unmuteVolume);
		}  else {
			setVolume((getVolume() + VOLUME_STEP) % 1.0f);
		}
	}

	private void decreaseVolume() {
		if (getVolume() == 0.0f) {
			// Unmute volume if muted
			setVolume(unmuteVolume);
		}  else {
			setVolume(getVolume() - VOLUME_STEP);
		}
	}

	protected void redoReacts() {}

	@EventSubscriber
	public void musicPlayerAction(ReactionAddEvent event) {
		if (event.getMessageID() == this.msg.getLongID() && event.getUser() != this.BOT.getOurUser()) {
			String emoji = event.getReaction().getEmoji().toString();
			if (emoji.equals(NAV_EMOJIS[0])) {
				// Resume playing
				resumePlayer();
			} else if (emoji.equals(NAV_EMOJIS[1])) {
				// Pause
				pausePlayer();
			} else if (emoji.equals(NAV_EMOJIS[2])) {
				// Stop
				stopPlayer();
			} else if (emoji.equals(NAV_EMOJIS[3])) {
				// Skip
				skip();
			} else if (emoji.equals(NAV_EMOJIS[4])) {
				// Song queue
				displayQueue();
			} else if (emoji.equals(NAV_EMOJIS[5])) {
				// Lock down access
				//restrictAccess(true);
			} else if (emoji.equals(NAV_EMOJIS[6])) {
				// Decrease volume
				decreaseVolume();
			} else if (emoji.equals(NAV_EMOJIS[7])) {
				// Increase volume
				increaseVolume();
			} else if (emoji.equals(NAV_EMOJIS[8])) {
				// Mute
				mute();
			}
		}
		this.yield();
	}

	@EventSubscriber
	public void unrestrictAccess(ReactionRemoveEvent event) {
		if (event.getMessageID() == this.msg.getLongID()) {
			String emoji = event.getReaction().getEmoji().toString();
			if (emoji.equals(NAV_EMOJIS[5])) {
				// Allow access
				//restrictAccess(false);
				this.yield();
			}
		}
	}

	@EventSubscriber
	public void playerReady(UserVoiceChannelJoinEvent event) {
		if (event.getUser() == this.BOT.getOurUser()) {
			this.msg = this.msg.edit(buildPlayer("", getPlayerBody("Connected"), Main.getParameter("PlayerReadyColor")));
			this.yield();
		}
	}

	@EventSubscriber
	public void disconnectPlayer(VoiceDisconnectedEvent event) {
		this.msg = this.msg.edit(buildPlayer("", getPlayerBody("Disconnected"), Main.getParameter("PlayerDisconnectedColor")));
		try {
			this.join();
		} catch (InterruptedException e) {}
	}

	@EventSubscriber
	public void setTitle(TrackStartEvent event) {
		IEmbed oldEmbed = this.msg.getEmbeds().get(0);
		File file = (File)event.getTrack().getMetadata().get("file");
		this.msg = this.msg.edit(buildPlayer(file.getName(), getPlayerBody("Playing"), oldEmbed.getColor().getRGB()));
	}

	@EventSubscriber
	public void clearTitle(TrackFinishEvent event) {
		IEmbed oldEmbed = this.msg.getEmbeds().get(0);
		this.msg = this.msg.edit(buildPlayer("", getPlayerBody("Stopped"), oldEmbed.getColor().getRGB()));
	}
}
