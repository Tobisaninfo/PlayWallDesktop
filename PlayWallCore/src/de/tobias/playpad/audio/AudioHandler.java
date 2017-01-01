package de.tobias.playpad.audio;

import java.nio.file.Path;

import de.tobias.playpad.pad.content.PadContent;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.util.Duration;

// Mögliche Interfaces: Equalizeable

/**
 * Describes an audio handler implantation.
 */
public abstract class AudioHandler {

	private PadContent content;

	public AudioHandler(PadContent content) {
		this.content = content;
	}

	public PadContent getContent() {
		return content;
	}

	/**
	 * Start the audio stream
	 */
	public abstract void play();

	/**
	 * Pause the audio stream.
	 */
	public abstract void pause();

	/**
	 * Stop the audio stream.
	 */
	public abstract void stop();

	/**
	 *	Get the current play position of the current player.
	 * @return current position
	 */
	public abstract Duration getPosition();

	/**
	 * Get the current play position of the current player.
 	 * @return current position property
	 */
	public abstract ReadOnlyObjectProperty<Duration> positionProperty();

	/**
	 * Get the duration of the current player.
	 *
	 * @return duration
	 */
	public abstract Duration getDuration();

	/**
	 * Get the duration of the current player.
	 *
	 * @return duration property
	 */
	public abstract ReadOnlyObjectProperty<Duration> durationProperty();

	/**
	 * Set the current volume between 0 and 1.
	 *
	 * @param volume new volume
	 */
	public abstract void setVolume(double volume);

	/**
	 * Check if media is loaded.
	 *
	 * @return <code>true</code> Loaded
	 */
	public abstract boolean isMediaLoaded();

	/**
	 * prepare a set of media to be played.
	 *
	 * @param paths path to the audio files
	 */
	public abstract void loadMedia(Path... paths);

	/**
	 * Unload Media to cleanup resources.
	 */
	public abstract void unloadMedia();

}
