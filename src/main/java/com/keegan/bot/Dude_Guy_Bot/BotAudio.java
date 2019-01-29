package com.keegan.bot.Dude_Guy_Bot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MissingPermissionsException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


public class BotAudio extends Command {

	private AudioPlayer audioPlayer;


	public BotAudio(String command) {
		this.command = command;
	}

	public void init(IMessage message, IDiscordClient bot) {
		super.init(message, bot);

		// Get the server's audioPlayer
		audioPlayer = AudioPlayer.getAudioPlayerForGuild(guild);
	}

	public void run() {

		// Check for permissions
		if (canIssueBotCommands()) {

			// Get arguments passed
			String[] args = getArgs();

			// Queue up a song from a url
			if (this.command.equals("queue")) {
				String url = processUrl(args[0]);

				if (url != null) {
					queue(url);
				} else {
					sendMessage("Invalid url");
				}
			}

			// Pause playback
			else if (this.command.equals("pause")) {
				audioPlayer.setPaused(true);
			}

			// Resume playback
			else if (this.command.equals("resume") || this.command.equals("play")) {
				audioPlayer.setPaused(false);
			}

			// Set channel as guild's music player interface channel
			else if (this.command.equals("player")) {
				//Main.writeServerSetting(, );
				IMessage playerMessage = sendMessage(EmbedPlayer.buildNewPlayer());
				EmbedPlayer player = new EmbedPlayer(botClient, playerMessage, cmdMessage.getAuthor());
			}
		}
	}

	private String processUrl(String url) {
		try {
			// Test if a valid url was passed
			String processedUrl = new URL(url).toString();

			// Check url domain
			if (processedUrl.contains("youtube.com")) {
				// YouTube url
				return processYtUrl(processedUrl, false);
			} else if (processedUrl.contains("youtu.be")) {
				// Shortened YouTube url
				return processYtUrl(processedUrl, true);
			} else if (processedUrl.contains("soundcloud.com") || processedUrl.endsWith(".webm")) {
				// Soundcloud url or direct link to .webm
				return processedUrl;
			}

			return null;
		} catch (Exception e) {}

		return null;
	}

	private String processYtUrl(String url, boolean shortened) {
		try {
			String processedUrl = url;

			// Expand shortened YouTube urls
		    if (shortened) {
		        String id = url.substring(url.lastIndexOf('/') + 1);
		        processedUrl = "https://www.youtube.com/watch?v=" + id;
		    }

			// Remove extra parameters in the url
			int cutoff = url.indexOf('&');
			if (cutoff == -1) {
				cutoff = processedUrl.length();
			}
			processedUrl = processedUrl.substring(0, cutoff);

			return processedUrl;
		} catch (Exception e) {}

		return null;
	}

	/**
	 * Attempt to queue a song to the AudioPlayer
	 *
	 * @param url The url of the song to queue
	 */
	private void queue(String url) {

		File songToQueue = dlSong(url);

		try {
			audioPlayer.queue(songToQueue);

			// Delete user's song request message
			cmdMessage.delete();

			String songName = songToQueue.getName().substring(0, songToQueue.getName().lastIndexOf('-'));
			sendTempMessage("💿🎶 **" + songName + "** queued");

			// Delete the file after queuing
			songToQueue.delete();
		} catch (Exception e) {
			Main.displayError(e + " occurred while queuing a song", e);
			sendTempMessage("An error occurred while queuing the song.");
		}

	}

    /**
     * This method downloads the source video/audio from the user-specified url, 
	 * if possible, using youtube-dl and converts it to mpeg3 if necessary, 
	 * then returns the final .mp3 file.
	 *
     * @param The source URL to download from
	 * @return The downloaded .mp3 file
     */
	private File dlSong(String url) {
		File song = null;
		try {
			// Download source and convert using youtube-dl
			String dlLocation = System.getProperty("user.dir") + "/data/Music/" + cmdMessage.getGuild().getLongID() + "/";
			String filenameFormat = "%(title)s-%(id)s.%(ext)s";
			String fileFormat = " --audio-format mp3 ";
			String audioQuality = " --audio-quality 128K ";
			String extraParams = " -x -q --no-progress";
			String dl_cmd = "youtube-dl " + url + 
				" -o " + dlLocation +
				filenameFormat + 
				fileFormat + 
				audioQuality + 
				extraParams;

			Process py = Runtime.getRuntime().exec(dl_cmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(py.getInputStream()));
			String input;
			input = in.readLine();
			py.waitFor();

			// Select the correct downloaded .mp3 file to return
			File dir = new File(dlLocation);
			for (File file : dir.listFiles()) {

				// Check the video id associated with the .mp3 file (YouTube & Niconico)
				if (file.getName().contains(url.toString().substring(url.toString().indexOf("watch") + 8))) {
				//if (file.getName().contains(url.substring(url.lastIndexOf('=') + 1))) {
					song = file;
				}
				
				// Check the sm id associated with the .mp3 file (Niconico)
				else if (file.getName().contains("sm")) {
					if (file.getName().contains(url.substring(url.lastIndexOf('/') + 1))) {
						song = file;
					}
				}
				
				// Check if .mp3 file contains the song name (Soundcloud)
				else if (url.contains("soundcloud.com")) {
					if (checkSoundcloudMP3(file.getName(), url)) {
						song = file;
					}
				}

				// Check if file contains Facebook video id
				else if (url.contains("facebook.com")) {
					if (file.getName().contains(url.substring(url.indexOf("videos/") + 7, url.length() - 1))) {
						song = file;
					}
				}

				// Check webm name
				else if (url.contains(".webm")) {
					System.out.println("Finding file containing: " + url.substring(url.lastIndexOf('/')+1, url.lastIndexOf('.')));
					if (file.getName().contains(url.substring(url.lastIndexOf('/')+1, url.lastIndexOf('.')))) {
						song = file;
					}
				}
			}

			/*
			if (song != null) {
				Main.displayMessage("Downloaded file found: " + song.getName());
			} else {
				Main.displayError("Downloaded file not found!");
			}
			*/
		} catch (Exception e) {
			Main.displayError(e + " occurred while downloading a song.", e);
		} finally {
			return song;
		}
	}

    /**
     * Checks if the .mp3 file contains words from the 
	 * SoundCloud link (contains the song name)
	 *
     * @param mp3Name The name of the .mp3 file
	 * @return A boolean that determines whether the .mp3 file is the correct soundcloud song to load
     */
    private boolean checkSoundcloudMP3(String mp3Name, String url) {

		boolean isCorrectSoundcloudMP3 = false;
		String tags = url.substring(url.lastIndexOf('/') + 1);

		for (String tag : tags.split("-")) {

			// The mp3 file MUST contain all of the song name tags in the url
        	if (mp3Name.toLowerCase().replace(".", "").contains(tag)) {
            	isCorrectSoundcloudMP3 = true;
			}
			else {
				isCorrectSoundcloudMP3 = false;
			}

        }

        return isCorrectSoundcloudMP3;
    }

	/**
	 * Return the command-specific help string to BotHelp
	 */
	public String getHelp() {
		String helpMessage = "";
		if (this.command.equals("queue")) {
			helpMessage += BotHelp.formatHelpMessage("queue", "url", "Queue up the song from the specified YouTube/Soundcloud URL");
		} else if (this.command.equals("pause")) {
			helpMessage += BotHelp.formatHelpMessage("pause", "Pauses the currently playing song");
		} else if (this.command.equals("resume")) {
			helpMessage += BotHelp.formatHelpMessage("resume", "Resumes playback of the current song, if paused");
		} else if (this.command.equals("play")) {
			helpMessage += BotHelp.formatHelpMessage("play", "Resumes playback of the current song, if paused");
		} else if (this.command.equals("skip")) {
			helpMessage += BotHelp.formatHelpMessage("skip", "Skips the currently playing song");
		} else if (this.command.equals("setvolume")) {
			helpMessage += BotHelp.formatHelpMessage("setvolume", "volume", "Sets the playback volume of the bot [0-100]");
		}

		return helpMessage;
	}

}
