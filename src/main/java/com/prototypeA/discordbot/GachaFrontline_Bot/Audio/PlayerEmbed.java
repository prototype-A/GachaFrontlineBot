package com.prototypeA.discordbot.GachaFrontline_Bot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.rest.util.Color;

import reactor.core.publisher.Mono;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.LinkedList;
import java.util.Queue;


public class PlayerEmbed {

	private static final String PLAY_BTN_ID_APPEND = "-PlayBtn";
	private static final String PAUSE_BTN_ID_APPEND = "-PauseBtn";
	private static final String SKIP_BTN_ID_APPEND = "-SkipBtn";
	private static final String TIME_BTN_ID_APPEND = "-TimeBtn";
	private static final String INFO_BTN_ID_APPEND = "-InfoBtn";

	private final Snowflake GUILD_ID;
	private final GatewayDiscordClient GATEWAY;
	private Message message;

	private AudioTrack currentTrack;
	private boolean playing;
	private Queue<AudioTrack> queuedTracks;

	private int skipVotes;
	private Queue<Member> skippedMembers;


	public PlayerEmbed(Message message, 
						GatewayDiscordClient gateway, AudioTrack currentTrack,
						boolean playing, Queue<AudioTrack> queuedTracks) {
		this.GUILD_ID = message.getGuild().block().getId();
		this.GATEWAY = gateway;

		this.currentTrack = currentTrack;
		this.playing = playing;
		this.queuedTracks = queuedTracks;

		this.skipVotes = 0;
		this.skippedMembers = new ConcurrentLinkedQueue(new LinkedList<>());

		// Create and send message
		this.message = message.getChannel().block()
						.createMessage(MessageCreateSpec.create()
							.withEmbeds(createEmbed())
							.withComponents(createButtons(this.playing))
						).share().block();

		// Button event handlers
		GATEWAY.on(ButtonInteractionEvent.class).subscribe(event -> {
			if (this.currentTrack != null) {
				String buttonId = event.getCustomId();

				if (buttonId.endsWith(PLAY_BTN_ID_APPEND)) {
					// Play button clicked
					event.deferEdit().block();
					GuildAudioManager.of(GUILD_ID).resumePlayback();
				} else if (buttonId.endsWith(PAUSE_BTN_ID_APPEND)) {
					// Pause button clicked
					event.deferEdit().block();
					GuildAudioManager.of(GUILD_ID).pausePlayback();
				} else if (buttonId.endsWith(SKIP_BTN_ID_APPEND)) {
					// Skip button clicked
					Member member = event.getInteraction().getMember().get();
					if (!skippedMembers.contains(member)) {
						event.deferEdit().block();

						skipVotes++;
						skippedMembers.add(member);

						if ((float)skipVotes / (getNumVoiceConnections() - 1) > 
							0.5f) {
							clearSkipList();
							GuildAudioManager.of(GUILD_ID).skipPlayback();
						} else {
							update(this.currentTrack, this.playing, 
									this.queuedTracks);
						}
					} else {
						event.reply("You have already voted to skip this track!")
							.withEphemeral(true).subscribe();
					}
				} else if (buttonId.endsWith(TIME_BTN_ID_APPEND)) {
					// Time left button clicked
					AudioTrackInfo trackInfo = this.currentTrack.getInfo();
					DecimalFormat df = new DecimalFormat("00");

					long currPos = this.currentTrack.getPosition();
					int posMin = getMins(currPos);
					String posSec = getSecsStr(currPos);

					long duration = trackInfo.length;
					int durMin = getMins(duration);
					String durSec = getSecsStr(duration);

					long timeLeft = duration - currPos;
					int minLeft = getMins(timeLeft);
					String secLeft = getSecsStr(timeLeft);

					String posIndicator = "●";
					String playbackBar = "━━━━━━━━━━━";
					int index = (int)(playbackBar.length() * currPos / duration);
					playbackBar = playbackBar.substring(0, index) + posIndicator + 
									playbackBar.substring(index, playbackBar.length());

					event.reply().withEmbeds(EmbedCreateSpec.builder()
									.title(trackInfo.title)
									.description("" + 
										posMin + ":" + posSec + " " + 
										playbackBar + " " + 
										durMin + ":" + durSec)
									.addField("Time Left", "" + 
										minLeft + ":" + secLeft, true)
								.build())
								.withEphemeral(true).subscribe();
				} else if (buttonId.endsWith(INFO_BTN_ID_APPEND)) {
					// Track info button clicked
					AudioTrackInfo trackInfo = this.currentTrack.getInfo();

					long currPos = this.currentTrack.getPosition();
					int posMin = getMins(currPos);
					String posSec = getSecsStr(currPos);

					long duration = trackInfo.length;
					int durMin = getMins(duration);
					String durSec = getSecsStr(duration);

					event.reply().withEmbeds(EmbedCreateSpec.builder()
										.title(trackInfo.title)
										.description(trackInfo.uri)
										.addField("Position", "" + 
											posMin + ":" + posSec, true)
										.addField("Duration", "" + 
											durMin + ":" + durSec, true)
									.build())
									.withEphemeral(true).subscribe();
				}
			} else {
				event.reply("There is currently no audio track playing")
						.withEphemeral(true).subscribe();
			}
		});
	}


	/**
	 * Updates the Discord message with updated playback, 
	 * queue, and status info
	 *
	 * @param currentTrack The currently playing audio track
	 * @param playing Whether the audio player is paused or not
	 * @param queuedTracks The playlist of the audio player
	 */
	public void update(AudioTrack currentTrack, boolean playing, 
						Queue<AudioTrack> queuedTracks) {
		if (currentTrack != this.currentTrack) {
			clearSkipList();
		}

		this.currentTrack = currentTrack;
		this.playing = playing;
		this.queuedTracks = queuedTracks;

		this.message = this.message.edit(MessageEditSpec.create()
							.withEmbeds(createEmbed())
							.withComponents(createButtons(this.playing))
						).share().block();
	}

	public void delete() {
		this.message.delete().subscribe();
	}


	/**
	 * Constructs the EmbedCreateSpec for creating the embed 
	 * in the message to be sent
	 *
	 * @return The spec to build the embed of the message
	 */
	private EmbedCreateSpec createEmbed() {

		// Title
		String embedTitle = ":stop_button: Stopped";

		// Embed colour
		Color embedColour = Color.of(Integer.parseInt(Main.getParameter("PlayerReadyColor")));

		EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder()
										.title(embedTitle)
										.color(embedColour);

		if (currentTrack != null) {
			AudioTrackInfo trackInfo = currentTrack.getInfo();

			// Title
			embedTitle = ((playing) ? ":arrow_forward: Playing: " : 
							":pause_button: Paused: ") + 
							trackInfo.title;
			embed = embed.title(embedTitle);

			// Requester
			Member member = currentTrack.getUserData(Member.class);
			String name = member.getUsername() + "#" + member.getDiscriminator();
			try {
				// Get nickname in server
				name = member.getNickname().get() + "#" + member.getDiscriminator();
			} catch (Exception e) {
				// No nickname in server
			}
			embed = embed.footer("Requested by: " + name, member.getAvatarUrl());

			// Duration
			int mins = getMins(trackInfo.length);
			String secs = getSecsStr(trackInfo.length);
			String trackLength = "" + mins + ":" + secs;
			embed = embed.addField("Duration", trackLength, true);
		}

		// Vote skip
		if (skipVotes > 0) {
			String skip = skipVotes + "/" + getNumVoiceConnections();
			embed = embed.addField("Skip", skip, true);
		}

		// Queued songs
		String queueList = "";
		int count = 0;
		for (AudioTrack track: queuedTracks) {
			queueList += track.getInfo().title + "\n";

			// Limit queued track list
			count++;
			if (count >= 3) {
				int numSongsInQueue = queuedTracks.size() - count;
				queueList += "(+" + numSongsInQueue + " more)";
				break;
			}
		}
		if (!queueList.equals("")) {
			embed = embed.addField("Up Next", queueList, false);
		}


		return embed.build();
	}

	/**
	 * Constructs the ActionRow that will contain the buttons for 
	 * controlling playback in the message to be sent
	 *
	 * @return The spec to build the embed of the message
	 */
	private ActionRow createButtons(boolean playing) {
		Button playButton = Button.secondary(GUILD_ID.asLong() +
												PLAY_BTN_ID_APPEND, "▶");
		Button pauseButton = Button.secondary(GUILD_ID.asLong() +
												PAUSE_BTN_ID_APPEND, "⏸");
		Button skipButton = Button.secondary(GUILD_ID.asLong() + 
												SKIP_BTN_ID_APPEND, "⏩︎");
		Button timeButton = Button.secondary(GUILD_ID.asLong() +
												TIME_BTN_ID_APPEND, "⌛︎");
		Button infoButton = Button.secondary(GUILD_ID.asLong() +
												INFO_BTN_ID_APPEND, "ℹ");

		ActionRow buttons = (playing && currentTrack != null) ? 
							ActionRow.of(pauseButton, skipButton, timeButton, 
								infoButton) :
							ActionRow.of(playButton, skipButton, timeButton, 
								infoButton);

		return buttons;
	}


	private int getNumVoiceConnections() {
		Snowflake voiceChannelId = GATEWAY.getVoiceConnectionRegistry()
									.getVoiceConnection(GUILD_ID).block()
									.getChannelId().block();
		return ((VoiceChannel)(message.getGuild().block()
					.getChannelById(voiceChannelId).block()))
				.getVoiceStates().count().block().intValue();
	}

	private void clearSkipList() {
		skipVotes = 0;
		skippedMembers = new ConcurrentLinkedQueue(new LinkedList<>());
	}

	private int getMins(long duration) {
		return (int)(duration / 60000);
	}

	private int getSecs(long duration) {
		return (int)(duration % 60000 / 1000);
	}

	private String getSecsStr(long duration) {
		DecimalFormat df = new DecimalFormat("00");
		return df.format(getSecs(duration));
	}
}
